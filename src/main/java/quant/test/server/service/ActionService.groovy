package quant.test.server.service

import quant.test.server.command.Command
import quant.test.server.log.Log
import quant.test.server.model.DeviceItem
import quant.test.server.model.TaskItem
import quant.test.server.model.TestCaseItem

/**
 * Created by cz on 2017/2/22.
 * 执行脚本的线程工作对象
 */
class ActionService implements Runnable{
    static final String TAG="ActionService"
    final TestCaseItem testCaseItem
    final DeviceItem deviceItem
    final TaskItem taskItem
    final String adbPath

    ActionService(String adbPath,TaskItem taskItem, DeviceItem deviceItem, TestCaseItem testCaseItem) {
        this.adbPath=adbPath
        this.taskItem = taskItem
        this.deviceItem = deviceItem
        this.testCaseItem = testCaseItem
    }

    @Override
    void run() {
        //执行脚本
        Command.shell("/Users/cz/Desktop/master/FxTestServer/src/main/resources/script/task.sh",
                [deviceItem.serialNumber,
                 deviceItem.toString(),
                 adbPath,testCaseItem.apkPath,
                 testCaseItem.testPath,0,-1,testCaseItem.name] as String[]){
            println it
            Log.e(TAG,it as String)
        }
    }
}
