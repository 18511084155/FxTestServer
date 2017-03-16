package quant.test.server.log

import quant.test.server.observable.DataObservable
import quant.test.server.prefs.FilePrefs
import quant.test.server.util.TextUtils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import java.util.stream.Collectors

/**
 * Created by cz on 2017/3/9.
 */
class LogProcessor extends Thread {
    private final int MAX_SIZE=300;//超过5000条被会保存
    private final LinkedList<LogItem> logItems=new LinkedList<>();
    private final ArrayList<LogItem> originalItems=new ArrayList<>();
    private final DataObservable observable=new DataObservable();
    LogProcessor() {
        super("log-thread");
    }

    @Override
    void run() {
        super.run();
        while(true){
            synchronized (logItems){
                try {
                    while(!logItems.isEmpty()){
                        LogItem logItem = logItems.pollFirst()
                        observable.notifyObservers(logItem)
                    }
                    //保存日志文件
                    if(MAX_SIZE<=originalItems.size()){
                        saveAllLog()
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    logItems.wait();
                }
            }
        }
    }

    /**
     * 保存所有的日志文件
     */
    public void saveAllLog() {
        def writer1, writer2
        //今天的起始时间
        LocalDate nowDate = LocalDate.now()
        LocalDate lastDate = nowDate.plusDays(-1)
        writer1 = new File(FilePrefs.LOG_FOLDER, nowDate.toString() + ".txt").newWriter(true)

        LocalDateTime localDateTime = LocalDateTime.of(nowDate, LocalTime.of(0, 0, 0))
        long timeMillis = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        for (Iterator<LogItem> iterator = originalItems.iterator(); iterator.hasNext();) {
            LogItem item = iterator.next()

            def logValue = item.toString() + "\n"
            if (item.ct < timeMillis) {
                //上一天内容
                writer2 ?: (writer2 = new File(FilePrefs.LOG_FOLDER, lastDate.toString() + ".txt").newWriter(true))
                writer2.write(logValue, 0, logValue.length())
            } else {
                //今天内容
                writer1.write(logValue, 0, logValue.length())
            }
            iterator.remove()
        }
        writer1?.close()
        writer2?.close()
    }

    void process(LogItem item) {
        synchronized (logItems){
            logItems.addLast(item);
            originalItems.add(item);
            logItems.notify();
        }
    }

    void registerObservable(Observer observer){
        this.observable.addObserver(observer);
    }

    void unregisterObservable(Observer observer){
        this.observable.deleteObserver(observer);
    }

    List<LogItem> filterLevel(int level){
        return originalItems.stream().filter({it.level >= level}).collect(Collectors.toList());
    }
    /**
     * 过滤文字
     * @param text
     * @param level
     */
    List<LogItem> filterLog(int level,String text){
        return originalItems.stream().
                filter({it.level >= level && (it.tag.contains(text) || it.value.contains(text))}).
                collect(Collectors.toList());
    }

    /**
     * 正则过滤条目
     * 传入正则有一次机率为非正则信息,若出现.则直接全部过滤
     * @param regex
     * @param level
     */
    List<LogItem> matcherLog(int level,String regex){
        Pattern pattern=null;
        try{
            pattern = Pattern.compile(regex);
        } catch (PatternSyntaxException e){
        }
        List<LogItem> logItems;
        if(null==pattern){
            logItems= Collections.emptyList();
        } else {
            final Pattern finalPattern=pattern;
            logItems = originalItems.stream().
                    filter({it.level >= level && (finalPattern.matcher(it.tag).find() || finalPattern.matcher(it.value).find())}).
                    collect(Collectors.toList());
        }
        return logItems;
    }

    /**
     * 检测一个条目是否,匹配所有条件
     * @param item
     * @param level
     * @param text
     * @param regex
     * @return
     */
    boolean matcherLog(LogItem item,int level,String text,boolean regex){
        boolean result=false;
        if(TextUtils.isEmpty(text)){
            //以等级过滤
            result=item.level >= level;
        } else {
            //以正则,或者关键字过滤
            if(!regex){
                result=item.level >= level && (item.tag.contains(text) || item.value.contains(text));
            } else {
                Pattern pattern=null;
                try{
                    pattern = Pattern.compile(text);
                } catch (PatternSyntaxException e){
                }
                if(null!=pattern){
                    result=item.level >= level && (pattern.matcher(item.tag).find() || pattern.matcher(item.value).find());
                }
            }
        }
        return result;
    }
}
