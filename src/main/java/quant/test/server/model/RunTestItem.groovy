package quant.test.server.model
import quant.test.server.service.ActionService
/**
 * Created by cz on 2017/3/8.
 */
class RunTestItem {
    final DeviceItem deviceItem
    final TestPlanItem testPlanItem
    final ActionService actionService

    RunTestItem(DeviceItem deviceItem, TestPlanItem testPlanItem, ActionService actionService) {
        this.deviceItem = deviceItem
        this.testPlanItem = testPlanItem
        this.actionService = actionService
    }
}
