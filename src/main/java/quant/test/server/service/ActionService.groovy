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

import java.time.*

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
    final int runCount
    def destroyed
    def callback
    def pid

    ActionService(String adbPath, DeviceItem deviceItem, TestPlanItem taskItem,TestCaseItem testCaseItem,int runCount) {
        this.adbPath=adbPath
        this.taskItem = taskItem
        this.deviceItem = deviceItem
        this.testCaseItem = testCaseItem
        this.runCount=runCount
    }

    @Override
    void run() {
        //校验执行脚本
        FileUtils.copyResourceFileIfNotExists(FilePrefs.SCRIPT_TASK, "script/task.sh");
        //脚本内,以秒计算
        long et=taskItem.et
        if(taskItem.cycle){
            def localDate=LocalDateTime.ofInstant(new Date(taskItem.et).toInstant(), ZoneId.systemDefault())
            //今天起始时间与,结束日期的当天时间值的时间差,因为et是最终结束时间,而我们需要拿到的是 如2017-3-9 12:30:00 后面的12:30:00的时间值
            def intervalTime=taskItem.et-new LocalDateTime(localDate.toLocalDate(),LocalTime.of(0,0)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            et=intervalTime+new LocalDateTime(LocalDate.now(),LocalTime.of(0,0)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
        Command.shell(FilePrefs.SCRIPT_TASK.absolutePath,//脚本位置
                [deviceItem.serialNumber,//手机序列id,用以区分执行多台设备
                 deviceItem.toString(),//手机标记名
                 adbPath,testCaseItem.apk1,//测试apk包位置
                 testCaseItem.apk2,//测试apk 用例位置
                 runCount,//运行次数
                 et/1000,//结束时间
                 taskItem.name,//测试计划名称
                 deviceItem.release] as String[]){
            def item=getMessage(it)
            if(!item||!item["type"]||!item["message"]){
                println "条目解析失败:$it"
            } else {
//              回调信息
                callback?.call(item)
//                //记录pid
                if(What.TASK.TYPE_INIT_PID==item.type){
                    pid=item.message
                    Log.i(TAG,"设备:${deviceItem.toString()} 启动任务:$taskItem.name PID:$pid")
                }
            }
        }
    }

    /**
     * 结束进程
     * @return
     */
    def destroy(){
        if(!destroyed){
            destroyed=true
            if(pid){
                def result=Command.exec("kill $pid")
                !result?: Log.i(TAG,"设备:${deviceItem.toString()} 执行任务:$taskItem.name 被终止 pid:$pid 执行结果:$result.exit")
            }
        }
    }

    def getMessage(String message){
        def item
        try{
            if("{"==message[0]&&"}"==message[-1]){
                item = new JsonSlurper().parseText(message)
            }
        } catch (Exception e){
            println "message:$message 解析失败!"
        }
        item
    }

    def actionCallback(callback){
        this.callback=callback
    }
}
