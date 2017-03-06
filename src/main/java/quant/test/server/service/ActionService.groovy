package quant.test.server.service

import groovy.json.JsonSlurper
import quant.test.server.command.Command
import quant.test.server.log.Log
import quant.test.server.model.DeviceItem
import quant.test.server.model.TestCaseItem
import quant.test.server.model.TestPlanItem
import quant.test.server.prefs.FilePrefs
import quant.test.server.protocol.What
import quant.test.server.util.FileUtils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
/**
 * Created by cz on 2017/2/22.
 * 执行脚本的线程工作对象
 */
class ActionService implements Runnable{
    static final String TAG="ActionService"
    final TestCaseItem testCaseItem
    final DeviceItem deviceItem
    final TestPlanItem taskItem
    final String adbPath
    def callback
    def pid

    ActionService(String adbPath, DeviceItem deviceItem, TestPlanItem taskItem,TestCaseItem testCaseItem) {
        this.adbPath=adbPath
        this.taskItem = taskItem
        this.deviceItem = deviceItem
        this.testCaseItem = testCaseItem
    }

    @Override
    void run() {
        //校验执行脚本
        FileUtils.copyResourceFileIfNotExists(FilePrefs.SCRIPT_TASK, "script/task.sh");
        //当天任务结束时间
        LocalDateTime todayTime=new LocalDateTime( LocalDate.now(),LocalTime.of(0,0,0))
        long todayStartTime=todayTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        long et=taskItem.cycle?todayStartTime+taskItem.st:taskItem.st
        Command.shell(FilePrefs.SCRIPT_TASK.absolutePath,
                [deviceItem.serialNumber,
                 deviceItem.toString(),
                 adbPath,testCaseItem.apk1,
                 testCaseItem.apk2,0,et,testCaseItem.name] as String[]){
            println it
            def item=getMessage(it)
            if(item){
                //回调信息
                callback?.call(it)
                //记录pid
                if(What.SCRIPT.TYPE_INIT_PID==item.type){
                    pid=item.message
                    Log.i(TAG,"任务:$taskItem.name 任务己启动.PID:$pid")
                }
            }
        }
    }

    /**
     * 结束进程
     * @return
     */
    def destroy(){
        !pid?:Command.exec("kill $pid")
    }

    def getMessage(String message){
        def item
        try{
            item = new JsonSlurper().parseText(message)
        } catch (Exception e){
            Log.e(TAG,"message:$line 解析失败!\n")
        }
        item
    }

    def actionCallback(callback){
        this.callback=callback
    }
}
