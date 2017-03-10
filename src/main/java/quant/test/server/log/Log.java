package quant.test.server.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Observer;

/**
 * Created by Administrator on 2017/2/16.
 */
public class Log {
    private static final LogProcessor processor=new LogProcessor();
    public static final int INFO=0;
    public static final int WARNING=1;
    public static final int ERROR=2;

    public static void startProcess(){
        processor.start();
    }


    public static final void i(String TAG,String info){
        processLog(INFO, TAG, info);
    }

    public static final void w(String TAG,String info){
        processLog(WARNING, TAG, info);
    }
    public static final void e(String TAG,String info){
        processLog(ERROR,TAG,info);
    }
    public static final void e(String TAG,Throwable e){
        processLog(ERROR,TAG,getStackTraceString(e));
    }

    private static void processLog(int level, String tag, String info) {
        processor.process(new LogItem(level, tag, info));
    }

    public static void registerObservable(Observer observer){
        processor.registerObservable(observer);
    }


    /**
     * 保存所有log日志
     */
    public static void saveAllLog(){
        processor.saveAllLog();
    }
    /**
     * 等级过滤
     * @param level
     */
    public static List<LogItem> filterLevel(int level){
        return processor.filterLevel(level);
    }

    /**
     * 过滤文字
     * @param text
     * @param level
     */
    public static List<LogItem> filterLog(int level,String text){
        return processor.filterLog(level, text);
    }

    /**
     * 正则过滤条目
     * @param regex
     * @param level
     */
    public static List<LogItem> matcherLog(int level,String regex){
        return processor.matcherLog(level, regex);
    }

    /**
     * 检测此日志是否可以匹配当前条件
     * @param logItem
     * @param level
     * @param text
     * @param regex
     * @return
     */
    public static boolean matcherLog(LogItem logItem,int level,String text,boolean regex){
        return processor.matcherLog(logItem, level, text, regex);
    }

    public static void unregisterObservable(Observer observer){
        processor.unregisterObservable(observer);
    }

    /**
     * Handy function to get a loggable stack trace from a Throwable
     * @param tr An exception to com.android.ddmlib.log
     */
    public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }
        Throwable t = tr;
        while (t != null) {
            if (t instanceof UnknownHostException) {
                return "";
            }
            t = t.getCause();
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }
}
