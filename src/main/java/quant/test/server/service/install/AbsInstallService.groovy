package quant.test.server.service.install

import quant.test.server.model.DeviceItem
import quant.test.server.model.TestPlanItem
/**
 * Created by cz on 2017/3/10.
 * android 5.0以上,辅助用户安装程序设定
 */
abstract class AbsInstallService {
    protected final DeviceItem deviceItem
    protected final TestPlanItem testPlanItem

    AbsInstallService(DeviceItem deviceItem,TestPlanItem testPlanItem) {
        this.deviceItem = deviceItem
        this.testPlanItem=testPlanItem
    }

    abstract void analysis()

    def findXmlNodeByText(file,textItems){
        Node node=new XmlParser().parse(file)
        final def findItems=[]
        eachNode(findItems,node){textItems.contains(it.value)}
        findItems
    }

    def eachNode(items,Node node,closure){
        def findItem=node.attributes().find { closure.call(it) }
        !findItem?:(items<<node)
        !node.children()?:node.children().each { eachNode(items,it,closure) }
    }
}
