package quant.test.server.log;

import java.io.PipedInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;

/**
 * Created by Administrator on 2017/2/16.
 */
public class Log {
    private static final LogProcessor processor=new LogProcessor();
    public static final int INFO=1;
    public static final int WARNING=2;
    public static final int ERROR=3;

    public static void startProcess(){
        processor.start();
    }


    public static final void i(String TAG,String info){
        processLog(INFO,TAG,info);
    }

    public static final void w(String TAG,String info){
        processLog(WARNING,TAG,info);
    }
    public static final void e(String TAG,String info){
        processLog(ERROR,TAG,info);
    }
    public static final void e(String TAG,Throwable e){
        processLog(ERROR,TAG,getStackTraceString(e));
    }

    public static final PipedInputStream newPipedStream(){
        return processor.newPipedStream();
    }

    public static final void destroyStream(){
        processor.destroyStream();
    }

    private static void processLog(int level, String tag, String info) {
        processor.process(new LogItem(level, tag, info));
    }

    /**
     * Handy function to get a loggable stack trace from a Throwable
     * @param tr An exception to log
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
