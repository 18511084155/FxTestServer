package quant.test.server.log;

import quant.test.server.observable.DataObservable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Observer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/2/16.
 */
public class LogProcessor extends Thread {
    private final LinkedList<LogItem> logItems=new LinkedList<>();
    private final ArrayList<LogItem> originalItems=new ArrayList<>();
    private final DataObservable observable=new DataObservable();
    public LogProcessor() {
        super("log-thread");
    }

    @Override
    public void run() {
        super.run();
        while(true){
            synchronized (logItems){
                try {
                    while(!logItems.isEmpty()){
                        LogItem logItem = logItems.pollFirst();
                        observable.notifyObservers(logItem);
                    }
                    logItems.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void process(LogItem item) {
        synchronized (logItems){
            logItems.addLast(item);
            originalItems.add(item);
            logItems.notify();
        }
    }

    public void registerObservable(Observer observer){
        this.observable.addObserver(observer);
    }

    public void unregisterObservable(Observer observer){
        this.observable.deleteObserver(observer);
    }

    /**
     * 过滤文字
     * @param text
     */
    public List<LogItem> filterLog(String text){
        return originalItems.stream().
                filter(item -> item.tag.contains(text) || item.value.contains(text)).
                collect(Collectors.toList());
    }

    /**
     * 正则过滤条目
     * @param regex
     */
    public List<LogItem> matcherLog(String regex){
        final Pattern pattern = Pattern.compile(regex);
        return originalItems.stream().
                filter(item -> pattern.matcher(item.tag).matches() || pattern.matcher(item.value).matches()).
                collect(Collectors.toList());
    }

}
