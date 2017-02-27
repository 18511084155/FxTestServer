package quant.test.server.event

import quant.test.server.model.TestPlanItem

/**
 * Created by cz on 2017/2/27.
 */
class OnTestPlanAddedEvent {
    final def TestPlanItem item

    OnTestPlanAddedEvent(TestPlanItem item) {
        this.item = item
    }
}
