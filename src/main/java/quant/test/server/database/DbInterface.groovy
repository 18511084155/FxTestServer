package quant.test.server.database

import quant.test.server.model.TestCaseItem
import quant.test.server.model.TestPlanItem

/**
 * Created by cz on 2017/2/27.
 */
interface DbInterface {

    void insertTestCase(TestCaseItem item)

    void deleteTestCase(TestCaseItem item)

    void updateTestCase(TestCaseItem item)

    List<TestCaseItem> queryTestCase()

    void insertTestPlan(TestPlanItem item)

    void deleteTestPlan(TestPlanItem item)

    void updateTestPlan(TestPlanItem item)

    List<TestPlanItem> queryTestPlan()
}
