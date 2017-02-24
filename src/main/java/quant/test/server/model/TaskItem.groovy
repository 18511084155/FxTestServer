package quant.test.server.model

/**
 * Created by cz on 2017/2/22.
 * 任务计划
 * 1:测试任务名称
 * 2:一次性任务/循环任务
 *
 *
 */
class TaskItem {
    int caseId;
    String name
    int uid
    long st
    long et
    boolean cycle
    boolean invalid
}
