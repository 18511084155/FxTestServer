package quant.test.server.service.install

import quant.test.server.command.Command
import quant.test.server.log.Log
import quant.test.server.model.DeviceItem
import quant.test.server.model.TestPlanItem
import quant.test.server.prefs.FilePrefs

/**
 * Created by cz on 2017/3/10.
 */
class XiaoMiInstallService extends AbsInstallService{
    final def TAG="XiaoMiInstallService"

    XiaoMiInstallService(DeviceItem deviceItem, TestPlanItem testPlanItem) {
        super(deviceItem, testPlanItem)
    }

    @Override
    void analysis() {
        File file=new File(FilePrefs.DUMP_FOLDER,"${deviceItem.toString().hashCode()}.xml")
        //文件存在
        if(file.exists()){
            def items=findXmlNodeByText(file,["继续安装","允许"])
            if(items){
                def textValue=items[0].attributes()["text"]
                def boundValue=items[0].attributes()["bounds"]
                if(boundValue){
                    def matcher=boundValue=~/\[(\d+),(\d+)\]\[(\d+),(\d+)\]/
                    if(matcher){
                        int left=matcher[0][1] as Integer
                        int top=matcher[0][2] as Integer
                        int right=matcher[0][3] as Integer
                        int bottom=matcher[0][4] as Integer
                        def centerX=(left+right)/2
                        def centerY=(top+bottom)/2
                        def result=Command.exec("adb -s $deviceItem.serialNumber shell input tap $centerX $centerY")
                        if(0<=result.exit){
                            Log.i(TAG,"点击:$textValue 位置:$boundValue 操作成功!")
                        } else {
                            Log.i(TAG,"点击:$textValue 位置:$boundValue 操作失败!")
                        }
                    }
                }
            }
            file.delete()//用完删除
        }
    }


}
