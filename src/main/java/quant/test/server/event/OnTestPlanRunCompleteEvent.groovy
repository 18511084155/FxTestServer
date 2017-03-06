package quant.test.server.event

import quant.test.server.model.TestPlanItem

/**
 * Created by cz on 2017/3/6.
 * 测试计划执行完毕事件
 */
class OnTestPlanRunCompleteEvent {
    final def TestPlanItem item

    OnTestPlanRunCompleteEvent(TestPlanItem item) {
        this.item = item
    }
}
