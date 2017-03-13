package quant.test.server.service
import groovy.json.JsonSlurper
import quant.test.server.command.Command
import quant.test.server.log.Log
import quant.test.server.model.DeviceItem
import quant.test.server.model.TestCaseItem
import quant.test.server.model.TestPlanItem
import quant.test.server.prefs.FilePrefs
import quant.test.server.protocol.What
import quant.test.server.service.install.DefaultInstallWorker
import quant.test.server.util.FileUtils
/**
 * Created by cz on 2017/3/10.
 * 安装脚本服务对象
 */
class InstallService implements Runnable{
    final def TAG="InstallService"
    final def DEFAULT_SERVICE="default"

    DefaultInstallWorker installWorker
    final TestCaseItem testCaseItem
    final DeviceItem deviceItem
    final TestPlanItem taskItem
    final String adbPath
    def destroyed
    def callback
    def pid

    InstallService(String adbPath, DeviceItem deviceItem, TestPlanItem taskItem,TestCaseItem testCaseItem) {
        this.adbPath=adbPath
        this.taskItem = taskItem
        this.deviceItem = deviceItem
        this.testCaseItem = testCaseItem
    }

    @Override
    void run() {
        //校验执行脚本
        FileUtils.copyResourceFileIfNotExists(FilePrefs.SCRIPT_INSTALL_APK, FilePrefs.SCRIPT_INSTALL_PATH);
        Command.shell(FilePrefs.SCRIPT_INSTALL_APK.absolutePath,//脚本位置
                [deviceItem.serialNumber,//手机序列id,用以区分执行多台设备
                 deviceItem.toString(),//手机标记名
                 adbPath,testCaseItem.apk1,//测试apk包位置
                 testCaseItem.apk2,//测试apk 用例位置
                 taskItem.name,
                 deviceItem.release] as String[]){
            def item=getMessage(it)
            if(!item||!item["type"]||!item["message"]){
                println "条目解析失败:$it $item"
            } else {
                callback?.call(item)
                switch (item.type){
                    case What.INSTALL.TYPE_LOG:
                        Log.e(TAG,item.message)
                        break;
                    case What.INSTALL.TYPE_INIT_PID:
                        this.pid=item.message
                        Log.i(TAG,"设备:${deviceItem.toString()} 启动安装服务,PID:$pid")
                        break;
                    case What.INSTALL.TYPE_INSTALL_CHECK:
                        //上传apk成功,开始检测,并操作
                        !installWorker?:installWorker.destroy()
                        installWorker=new DefaultInstallWorker(deviceItem,adbPath)
                        installWorker.start()
                        break;
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
            !installWorker?:installWorker.destroy()
            if(pid){
                def result=Command.exec("kill $pid")
                !result?: Log.i(TAG,"当前设备:${deviceItem.toString()} 安装服务被终止 pid:$pid 执行结果:$result.exit")
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
