package quant.test.server.callback;

import quant.test.server.model.DeviceItem;
import quant.test.server.model.TestCaseItem;
import quant.test.server.model.TestPlanItem;

/**
 * Created by cz on 2017/3/7.
 */
public interface TestPlanCallback {
    void runTestPlan(TestPlanItem item,TestPlanItem current);
    void startTaskAction(String sdkPath,DeviceItem deviceItem,TestPlanItem item,TestCaseItem testCaseItem);
}
