package quant.test.server.model
/**
 * Created by cz on 2017/2/22.
 * 任务计划
 * 1:测试任务名称
 * 2:一次性任务/循环任务
 *
 *
 */
class TestPlanItem {
    int caseId
    String name
    String testCase
    String startDate
    String endDate
    boolean cycle
    boolean invalid
    int uid
    long st
    long et

    @Override
    String toString() {
        return name
    }
}
