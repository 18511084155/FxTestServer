package quant.test.server.service.install
import groovy.json.JsonSlurper
import quant.test.server.command.Command
import quant.test.server.log.Log
import quant.test.server.model.DeviceItem
import quant.test.server.prefs.FilePrefs
import quant.test.server.protocol.What
import quant.test.server.util.FileUtils

/**
 * Created by cz on 2017/3/10.
 * android 5.0以上,辅助用户安装程序设定
 */
abstract class AbsInstallWorker extends Thread{
    final DeviceItem deviceItem
    final String adbPath
    def pid

    AbsInstallWorker(DeviceItem deviceItem,String adbPath) {
        this.deviceItem=deviceItem
        this.adbPath=adbPath
    }

    def dumpUiFile(){
        def dumpFile=new File(FilePrefs.DUMP_FOLDER,deviceItem.toString().hashCode()+".xml")
        dumpFile.delete()
        FileUtils.copyResourceFileIfNotExists(FilePrefs.SCRIPT_INSTALL_CHECK,FilePrefs.SCRIPT_INSTALL_CHECK_PATH)
        int exitValue=Command.shell(FilePrefs.SCRIPT_INSTALL_CHECK.absolutePath,//脚本位置
                [deviceItem.serialNumber,//手机序列id,用以区分执行多台设备
                 deviceItem.toString(),//手机标记名
                 adbPath, dumpFile.absolutePath] as String[]){
            def item=getMessage(it)
            if(item){
                if(What.TASK.TYPE_INIT_PID==item.type){
                    pid=item.message
                } else if(What.TASK.TYPE_LOG==item.type){
                    Log.i(TAG,item.message)
                }
            }
        }
        exitValue
    }

    def findXmlNodeByText(file,textItems){
        final def findItems=[]
        if(file.exists()){
            Node node=new XmlParser().parse(file)
            eachNode(findItems,node){textItems.contains(it.value)}
            findItems
        }
    }

    def eachNode(items,Node node,closure){
        def findItem=node.attributes().find { closure.call(it) }
        !findItem?:(items<<node)
        !node.children()?:node.children().each { eachNode(items,it,closure) }
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
}
