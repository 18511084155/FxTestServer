package quant.test.server.service.install

import quant.test.server.command.Command
import quant.test.server.log.Log
import quant.test.server.model.DeviceItem
import quant.test.server.prefs.FilePrefs
import quant.test.server.prefs.PrefsKey
import quant.test.server.prefs.SharedPrefs

/**
 * Created by cz on 2017/3/10.
 */
class DefaultInstallWorker extends AbsInstallWorker{
    final def TAG="DefaultInstallWorker"
    final def adbPath
    def destroyed
    DefaultInstallWorker(DeviceItem deviceItem,String envPath) {
        super(deviceItem,envPath)
        adbPath=SharedPrefs.get(PrefsKey.ADB)
    }

    @Override
    void run() {
        super.run()
        analysis()
    }

    void analysis(){
        int exitValue=dumpUiFile()
        def dumpFile=new File(FilePrefs.DUMP_FOLDER,deviceItem.toString().hashCode()+".xml")
        if(!destroyed){
            if(0!=exitValue){
                //重新请求
                analysis()
            } else {
                //获取到文件
                if(!dumpFile.exists()){
                    Log.i(TAG,"执行成功但文件不存在,请检测!")
                } else {
                    def items=findXmlNodeByText(dumpFile,["继续安装","允许"])
                    if(!items) {
                        //没找到,继续找
                        Log.i(TAG, "${deviceItem.toString()}查找安装节点失败!")
                        analysis()
                    } else {
                        def textValue=items[0].attributes()["text"]
                        def boundValue=items[0].attributes()["bounds"]
                        if(boundValue){
                            def matcher=boundValue=~/\[(\d+),(\d+)\]\[(\d+),(\d+)\]/
                            if(matcher){
                                int left=matcher[0][1] as Integer
                                int top=matcher[0][2] as Integer
                                int right=matcher[0][3] as Integer
                                int bottom=matcher[0][4] as Integer
                                int centerX=(left+right)/2
                                int centerY=(top+bottom)/2
                                def result=Command.exec("$adbPath -s $deviceItem.serialNumber shell input tap $centerX $centerY")
                                Log.i(TAG,"点击按钮:$textValue x:$centerX y:$centerY 结果:$result.exit")
                            }
                        }
                        dumpFile.delete()//用完删除
                    }
                }
            }
        }
    }

    @Override
    void destroy() {
        if(!destroyed) {
            destroyed = true
            if (pid) {
                def result = Command.exec("kill $pid")
                !result ?: Log.i(TAG, "设备:${deviceItem.toString()} 安装检测对象被中止 pid:$pid 执行结果:$result.exit")
            }
        }
    }
}
