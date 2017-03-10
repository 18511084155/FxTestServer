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
        //脚本内,以秒计算
        long et=(taskItem.cycle?todayStartTime+taskItem.et:taskItem.et)/1000
        def dumpFile=new File(FilePrefs.DUMP_FOLDER,deviceItem.toString().hashCode()+".xml")
        Command.shell(FilePrefs.SCRIPT_TASK.absolutePath,//脚本位置
                [deviceItem.serialNumber,//手机序列id,用以区分执行多台设备
                 deviceItem.toString(),//手机标记名
                 adbPath,testCaseItem.apk1,//测试apk包位置
                 testCaseItem.apk2,//测试apk 用例位置
                 0,//运行次数
                 et,//结束时间
                 taskItem.name,//测试计划名称,下面为导出手机界面元素文件到本机位置
                 dumpFile.absolutePath,
                 deviceItem.release] as String[]){
            def item=getMessage(it)
            if(item){
//                //回调信息
                callback?.call(item)
//                //记录pid
                if(What.SCRIPT.TYPE_INIT_PID==item.type){
                    pid=item.message
                    Log.i(TAG,"任务:$taskItem.name 己启动.PID:$pid")
                }
            }
        }
    }

    /**
     * 结束进程
     * @return
     */
    def destroy(){
        if(pid){
            def result=Command.exec("kill $pid")
            !result?: Log.i(TAG,"当前任务:$taskItem.name 被终止 pid:$pid 执行结果:$result.exit")
        }
    }

    def getMessage(String message){
        def item
        try{
            item = new JsonSlurper().parseText(message)
        } catch (Exception e){
            println "message:$message 解析失败!"
        }
        item
    }

    def actionCallback(callback){
        this.callback=callback
    }
}
