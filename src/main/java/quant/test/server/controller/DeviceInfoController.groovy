package quant.test.server.controller
import com.jfoenix.controls.*
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import quant.test.server.anntation.FXMLLayout
import quant.test.server.bus.RxBus
import quant.test.server.database.DbHelper
import quant.test.server.event.OnDeviceConnectedEvent
import quant.test.server.event.OnDeviceDisConnectedEvent
import quant.test.server.event.OnDeviceSelectedEvent
import quant.test.server.event.OnTestPlanAddedEvent
import quant.test.server.log.Log
import quant.test.server.model.DeviceItem
import quant.test.server.model.DeviceProperty
import quant.test.server.model.Property
import quant.test.server.model.TestPlanItem
import quant.test.server.prefs.FilePrefs
import quant.test.server.prefs.PrefsKey
import quant.test.server.prefs.SharedPrefs
import quant.test.server.protocol.What
import quant.test.server.service.ActionService
import quant.test.server.util.FileUtils
import quant.test.server.widget.TestPlanCell
import rx.Observable
import rx.Subscriber
import rx.Subscription
import rx.schedulers.Schedulers

import javax.annotation.PreDestroy
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.Executors
/**
 * Created by cz on 2017/2/16.
 */
@FXMLLayout("fxml/device_info_layout.fxml")
class DeviceInfoController implements Initializable{
    final static def TAG='DeviceInfoController'
    static final long MIN_INTERVAL_TIME=4*60*1000

    @FXML Label mobileModel
    @FXML Label mobileSerialno
    @FXML Label mobileImei
    @FXML Label mobileVersion
    @FXML Label mobileIpAddress

    @FXML Label testPlanName
    @FXML Label testCaseName
    @FXML Label testStartTime
    @FXML Label testEndTime
    @FXML Label nextTestPlan
    @FXML Label nextTestStartTime
    @FXML Label nextTestEndTime

    @FXML JFXTextField searchField
    @FXML JFXTreeTableView treeTableView
    @FXML JFXTreeTableColumn deviceKey
    @FXML JFXTreeTableColumn deviceValue

    @FXML JFXListView testPlanList


    final def deviceProperties = FXCollections.observableArrayList()
    final def threadPool = Executors.newCachedThreadPool()
    final Map<TestPlanItem,ActionService> runPlanItems=[:]//正在执行列
    final def actionItems=[:]//正在任务列
    final List<DeviceItem> deviceItems=[]//当前连接设备列表
    TestPlanItem currentPlan
    def planItems=[]//当前计划集

    @Override
    void initialize(URL location, ResourceBundle resources) {
        //初始化测试计划列
        initEvent()
        initTestPlanList()
        queryAndInitTestPlanItems()
        initPropertyTable()
    }

    /**
     * 初始化事件
     */
    private void initEvent() {
        //选中设备事件
        RxBus.subscribe(OnDeviceSelectedEvent.class) {
            mobileModel.setText(it.item.toString())
            mobileSerialno.setText(it.item.serialNumber)
            mobileImei.setText(it.item.getDeviceProperty(Property.DEVICE_IMEI))
            mobileVersion.setText(it.item.sdk)
            mobileIpAddress.setText(it.item.getDeviceProperty(Property.DEVICE_ADDRESS))

            deviceProperties.clear()
            it.item.deviceProperty.each { property ->
                deviceProperties.add(new DeviceProperty(property.key, property.value))
            }
            if (Platform.fxApplicationThread) {
                treeTableView.setRoot(new RecursiveTreeItem<DeviceProperty>(deviceProperties, { it.getChildren() }))
                treeTableView.setShowRoot(false)
            } else {
                Platform.runLater({
                    treeTableView.setRoot(new RecursiveTreeItem<DeviceProperty>(deviceProperties, { it.getChildren() }))
                    treeTableView.setShowRoot(false)
                })
            }
        }
        //动态添加任务计划事件
        RxBus.subscribe(OnTestPlanAddedEvent.class) {
            //加入计划
            def items=[it.item]
            items+=planItems
            initTestPlanItems(items)
        }
        //设备连接
        RxBus.subscribe(OnDeviceConnectedEvent.class){
            deviceItems<<it.deviceItem
            //这里检测,当前是否有任务执行.如果有.则启动
            !currentPlan?:threadPool.execute({executeAction(currentPlan)})
        }
        //设备断开
        RxBus.subscribe(OnDeviceDisConnectedEvent.class){
            deviceItems.remove(it.deviceItem)
        }
    }

    private void initPropertyTable() {
        deviceKey.setCellValueFactory({
            deviceKey.validateValue(it) ? it.value.value.key : deviceKey.getComputedValue(it)
        })
        deviceValue.setCellValueFactory({
            deviceValue.validateValue(it) ? it.value.value.value : deviceValue.getComputedValue(it)
        })


        searchField.textProperty().addListener({ o, oldVal, newVal ->
            treeTableView.setPredicate({ property -> property.value.key.get().contains(newVal) || property.value.value.get().contains(newVal) })
        } as ChangeListener<String>)
    }

    /**
     * 初始化测试计划条目
     * @return
     */
    private Subscription queryAndInitTestPlanItems() {
        Observable.create({ Subscriber subscriber ->
            subscriber.onNext(DbHelper.helper.queryTestPlan())
            subscriber.onCompleted()
        } as Observable.OnSubscribe<List<TestPlanItem>>).
                subscribeOn(Schedulers.io()).
                subscribe({ items -> !items?: initTestPlanItems(items) }, { it.printStackTrace() })
    }

    /**
     * 初始化测试计划条目
     * @param items
     */
    private void initTestPlanItems(items) {
        //1:重新排序
        planItems.clear()
        planItems += sortTestPlanItems(items)
        //2:检测当前有无任务可以立即执行: 当前时间为12:00 9:00-13:00 cycle 11:-13:00 not cycle
        def runItems = findRunPlanItems()
        //3:有马上可以运行计划,且任务数大于2
        if(runPlanItems){
            //4:有可执行条目,检测是否替换为优先级高的任务
            loopNewPlan(currentPlan)
        } else {
            //当前没有可执行条目
            if (runItems) {
                //5:重新检测任务执行时间,若循环任务执行与下一个一性次任务间隔时间非常短,如2分钟,则放弃执行周期任务,因为一次性任务执行优先级更高
                //12:01  11:30-13:00 11:35-13:00 not cycle
                if (1 == runItems.size()) {
                    Platform.runLater({ runTestPlan(planItems[0]) })
                } else {
                    long testPlanTime1 = getTestPlanTime(runItems[0], runItems[0].st)
                    long testPlanTime2 = getTestPlanTime(runItems[1], runItems[1].st)
                    if (testPlanTime1 - testPlanTime2 < MIN_INTERVAL_TIME && !runItems[0]&&!runItems[1].cycle) {
                        Platform.runLater({ runTestPlan(planItems[1]) })
                    } else {
                        Platform.runLater({ runTestPlan(planItems[0]) })
                    }
                }
            } else if (!runItems) {
                //6:找下一个时间距离最近的任务,当前没有可执行任务,检测可循环任务
                Platform.runLater({ runTestPlan(planItems[0]) })
            }
        }
        testPlanList.getItems().clear()
        testPlanList.getItems().addAll(planItems)
    }

    def sortTestPlanItems(items){
        items.sort{TestPlanItem t1,TestPlanItem t2->
            long startTime1=getTestPlanTime(t1,t1.st)
            long startTime2=getTestPlanTime(t2,t2.st)
            startTime1-startTime2
        } as Comparator<TestPlanItem>
    }

    /**
     * 找查当前可以立即运行的条目
     * @return
     */
    def findRunPlanItems(){
        final long todayTime=LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        planItems.findAll { TestPlanItem item->
            long startTime=getTestPlanTime(item,item.st)
            long endTime=getTestPlanTime(item,item.et)
            startTime<todayTime&&endTime>todayTime
        }
    }

    /**
     * 初始化测试计划列表
     * @return
     */
    def initTestPlanList(){
        testPlanList.setItems(FXCollections.observableArrayList())
        testPlanList.setCellFactory({ new TestPlanCell() })
    }


    /**
     * 获得任务计划的计算时间,因为循环任务记录仅为当天秒值
     * @param item 计划条目
     * @param time 任务时间
     * @return
     */
    long getTestPlanTime(item,time){
        LocalDateTime todayTime=new LocalDateTime( LocalDate.now(),LocalTime.of(0,0,0))
        long todayStartTimeMillis=todayTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        item.cycle?todayStartTimeMillis+time:time
    }



    def getNextTestPlanItem(item){
        int index=planItems.indexOf(item)
        def nextItem=null
        if(-1<index&&index<planItems.size()-1){
            nextItem=planItems[index+1]
        }
        nextItem
    }

    /**
     * 初始化执行任务计划
     * @param item
     */
    def runTestPlan(TestPlanItem item) {
        if(item){
            currentPlan=item
            testPlanName.setText(item.name)
            testCaseName.setText(item.testCase)
            testStartTime.setText(item.startDate)
            testEndTime.setText(item.endDate)
            //检测用户任务运行状态
            if(!runPlanItems){
                //当前不存在执行任务,直接执行
                threadPool.execute({executeAction(item)})
            } else {
                //TODO 存在执行任务.检测任务
            }
        }
        //下一个执行任务
        TestPlanItem nextItem=getNextTestPlanItem(item)
        if(nextItem){
            nextTestPlan.setText(nextItem.name)
            nextTestStartTime.setText(nextItem.startDate)
            nextTestEndTime.setText(nextItem.endDate)
        }
    }

    /**
     * 运行计划任务
     * @param item
     * @return
     */
    def executeAction(TestPlanItem item){
        FileUtils.copyResourceFileIfNotExists(FilePrefs.SCRIPT_TASK, "script/task.sh");
        def testCaseItem=DbHelper.helper.queryTestCase(item.caseId)
        if(!testCaseItem){
            //脚本记录不存在,可能为人为删除
            Log.e(TAG,"执行任务时,检测到用例:$item.testCase ID:$item.caseId 不存在!")
        } else {
            final String sdkPath=SharedPrefs.get(PrefsKey.SDK)
            deviceItems.each {
                def actionService=new ActionService(sdkPath,it,item,testCaseItem)
                actionService.actionCallback{ processMessage(item,it) }
                actionItems<<[(item):actionService]
                threadPool.execute(actionService)
            }
        }
    }




    /**
     * 处理消息
     * @param message
     */
    def processMessage(TestPlanItem testPlanItem,item){
        //记录信息
        switch (item.type){
            case What.SCRIPT.TYPE_INSTALL_SUCCESS:
                Log.e(TAG,"当前任务:$testPlanItem.name $item.message 安装成功!")
                break
            case What.SCRIPT.TYPE_INSTALL_FAILED:
                Log.e(TAG,"当前任务:$testPlanItem.name $item.message 安装失败!")
                break
            case What.SCRIPT.TYPE_MD5_ERROR:
                Log.e(TAG,"当前任务:$testPlanItem.name $item.message")
                break
            case What.SCRIPT.TYPE_LOG:
                Log.i(TAG,item.message)
                break
            case What.SCRIPT.TYPE_RUN_LOOP:
                //遍历新的可执行计划
                loopNewPlan(testPlanItem)
                break
            case What.SCRIPT.TYPE_RUN_COMPLETE:
                //执行完毕
                def nextPlan=getNextTestPlanItem(testPlanItem)
                //更新新任务信息
                !nextPlan?:runTestPlan(nextPlan)
                break
        }
    }

    /**
     * 遍历新的可执行计划
     * @return
     */
    def loopNewPlan(TestPlanItem item){
        def newPlan=queryTestPlanItems()
        if(newPlan){
            //有了新的执行任务,结束当前任务
            runPlanItems[item]?.destroy()
            //更新新任务信息
            runTestPlan(newPlan)
        }
    }

    /**
     * 检测当前测试计划是否有变动,如中间插入一条记录,需要优先执行
     */
    def queryTestPlanItems(){
        def newPlan
        if(planItems&&currentPlan){
            if(!currentPlan.cycle){
                def runPlanItems=findRunPlanItems()
                if(runPlanItems){
                    runPlanItems-=currentPlan
                    if(runPlanItems){
                        newPlan=runPlanItems[0]
                    }
                }
            }
        }
        newPlan
    }

    @PreDestroy
    public void destroy(){
        RxBus.unSubscribeItems(this)
    }


}
