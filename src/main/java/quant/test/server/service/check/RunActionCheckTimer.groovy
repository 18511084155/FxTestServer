package quant.test.server.service.check

import javafx.application.Platform
import quant.test.server.callback.TestPlanCallback
import quant.test.server.log.Log
import quant.test.server.model.DeviceItem
import quant.test.server.model.TestCaseItem
import quant.test.server.model.TestPlanItem

import java.time.LocalDateTime
import java.time.ZoneId
/**
 * Created by cz on 2017/3/11.
 * 此对象,检测所有当前正在执行任务的动态执行情况,主要解决场景
 * 任务时间过长.导致进程在运行一段时间后,无响应了.
 * 实现步骤:当进程每执行一次时,会输出一个loop信息,
 * 此时,记录每次执行时间,然后为下一次执行,设一个延持任务.
 * 如果到了时间仍未执行.说明进程可能出现问题,这时重启进程
 *
 */
class RunActionCheckTimer extends Timer {
    final def TAG="RunActionCheckTimer"
    final TestPlanCallback callback
    final TestCaseItem testCaseItem
    final DeviceItem deviceItem
    final TestPlanItem taskItem
    final String adbPath

    TimerTask checkTask
    final def timeItems=[]//每次执行时间
    def meanInterval//平均间隔
    def interval //执行间隔


    RunActionCheckTimer(TestPlanCallback callback,String adbPath, DeviceItem deviceItem, TestPlanItem taskItem,TestCaseItem testCaseItem) {
        this.callback=callback
        this.adbPath=adbPath
        this.taskItem = taskItem
        this.deviceItem = deviceItem
        this.testCaseItem = testCaseItem
    }

    /**
     * 记录当前运行时间
     */
    def addActionTime(){
        long nowTimeMillis=LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if(0<timeItems.size()){
            //记录间隔
            interval=nowTimeMillis-timeItems[-1]
        }
        timeItems<<nowTimeMillis
        //并计算平衡间隔
        meanInterval=(timeItems[-1]-timeItems[0])/timeItems.size()
    }

    /**
     * 重置任务
     * @return
     */
    def resetTask() {
        //重置定时器
        if (interval) {
            if(checkTask){
                checkTask.cancel()
                purge()
            }
            //启动一个定时任务
            long nowTimeMillis = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            def date = new Date(nowTimeMillis + interval * 2)
            checkTask={
                Log.e(TAG,"检测到设备:${deviceItem.toString()} 任务:$taskItem.name 任务异常,重启任务!")
                Platform.runLater({callback.startTaskAction(adbPath,deviceItem,taskItem,testCaseItem)});
            } as TimerTask
            schedule(checkTask, date)
        }
    }

    def reset(){
        interval=null
        meanInterval=null
        timeItems.clear()
    }

}
