package quant.test.server.service;

import javafx.application.Platform;
import quant.test.server.callback.TestPlanCallback;
import quant.test.server.model.TestPlanItem;

import java.util.TimerTask;

/**
 * Created by cz on 2017/3/7.
 */
public class TestPlanTimerTask extends TimerTask {
    final TestPlanCallback callback;
    final TestPlanItem item;
    final TestPlanItem current;
    boolean isRunning;

    public TestPlanTimerTask(TestPlanCallback callback,TestPlanItem item,TestPlanItem current) {
        this.callback=callback;
        this.item = item;
        this.current=current;
    }

    @Override
    public void run() {
        isRunning=true;
        Platform.runLater(() -> callback.runTestPlan(item,current));
    }

    public boolean isRunning() {
        return isRunning;
    }
}
