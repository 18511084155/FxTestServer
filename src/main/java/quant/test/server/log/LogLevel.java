package quant.test.server.log;

/**
 * Created by cz on 2017/2/17.
 */
public enum LogLevel {
    I(0), W(1), E(2);
    public int level;

    LogLevel(int level) {
        this.level = level;
    }
}
