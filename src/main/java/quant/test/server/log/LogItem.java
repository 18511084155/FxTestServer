package quant.test.server.log;

/**
 * Created by Administrator on 2017/2/16.
 */
public class LogItem {
    public final long ct;
    public final int level;
    public final String tag;
    public final String value;
    public final String threadName;

    public LogItem(int level,String tag, String value) {
        this.tag = tag;
        this.level=level;
        this.value = value;
        this.ct = System.currentTimeMillis();
        this.threadName=Thread.currentThread().getName();
    }
}
