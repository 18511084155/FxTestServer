package quant.test.server.exception

import quant.test.server.bus.RxBus
import quant.test.server.event.OnExceptionHandleEvent

/**
 * Created by cz on 12/9/16.
 */
class ExceptionHandler implements java.lang.Thread.UncaughtExceptionHandler {
    @Override
    void uncaughtException(Thread t, Throwable e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        RxBus.post(new OnExceptionHandleEvent(stackTrace.toString()))
        new File("${System.currentTimeMillis()}.txt").withWriter {it.write(stackTrace.toString()) }
    }
}
