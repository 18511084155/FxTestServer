package quant.test.server.event

import quant.test.server.model.TestCaseItem

/**
 * Created by cz on 2017/2/27.
 */
class OnTestCaseAddedEvent {
    final TestCaseItem testCaseItem

    OnTestCaseAddedEvent(testCaseItem) {
        this.testCaseItem = testCaseItem
    }
}
