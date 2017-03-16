package quant.test.server.model

import quant.test.server.callback.TestPlanCallback
import quant.test.server.service.ActionService
import quant.test.server.service.InstallService
import quant.test.server.service.check.RunActionCheckTimer

/**
 * Created by cz on 2017/3/8.
 */
class RunTestItem {
    final DeviceItem deviceItem
    final TestPlanItem testPlanItem
    final TestCaseItem testCaseItem
    final RunActionCheckTimer checkTask
    InstallService installService
    ActionService actionService
    def destroyed

    RunTestItem(TestPlanCallback callback,String adbPath,DeviceItem deviceItem, TestPlanItem testPlanItem,TestCaseItem testCaseItem, InstallService installService) {
        this.deviceItem = deviceItem
        this.testPlanItem = testPlanItem
        this.testCaseItem = testCaseItem
        this.installService = installService
        this.checkTask=new RunActionCheckTimer(callback,adbPath,deviceItem,testPlanItem,testCaseItem)
    }

    int getRunCount(){
        this.checkTask.timeItems.size()
    }

    def reset(){
        if(installService){
            installService.destroy()
            installService=null
        }
        if(actionService){
            actionService.destroy()
            actionService=null
        }
        this.checkTask.reset()
    }

    def destroy(){
        if(!destroyed){
            destroyed=true
            if(installService){
                installService.destroy()
                installService=null
            }
            if(actionService){
                actionService.destroy()
                actionService=null
            }
            this.checkTask.cancel()
        }
    }

}
