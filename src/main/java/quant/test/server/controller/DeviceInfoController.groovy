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
import quant.test.server.callback.TestPlanCallback
import quant.test.server.command.Command
import quant.test.server.database.DbHelper
import quant.test.server.event.*
import quant.test.server.log.Log
import quant.test.server.model.*
import quant.test.server.prefs.FilePrefs
import quant.test.server.prefs.PrefsKey
import quant.test.server.prefs.SharedPrefs
import quant.test.server.protocol.What
import quant.test.server.service.ActionService
import quant.test.server.service.InstallService
import quant.test.server.service.TestPlanTimerTask
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
 * 因为手机动态的连接因素,所以设计以下以点
 * 1:当手机动态插入时,这时候己有一个任务执行
 * 2:手机动态移除adb连接时,这时己有任务执行
 *
 * 任务动态添加时
 *  1:己有任务执行,检测当前执行任务,是否为循环任务.优先级如果低于一次性任务,则重新执行一次性任务
 *  2:没有任务动态执行,检测当前是否拥有执行任务.如果没有,等待执行,如果可以直接执行,则直接执行
 *
 * 2017/3/6,遗留任务:
 * 1:没有任务执行时,任务定时处理
 * 2:Udb连接问题
 * 3:RxJava的线程调度
 */
@FXMLLayout("fxml/device_info_layout.fxml")
class DeviceInfoController implements Initializable,TestPlanCallback{
    final static def TAG='DeviceInfoController'
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


    static final List<RunTestItem> runTestItems =[]//执行任务集
    final def deviceProperties = FXCollections.observableArrayList()
    final def threadPool = Executors.newCachedThreadPool()
    final List<DeviceItem> deviceItems=[]//当前连接设备列表
    final Timer timer=new Timer()//启动任务定时器
    TestPlanTimerTask testPlanTimerTask//未来的任务计划
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
        RxBus.subscribe(OnDeviceAdbConnectedEvent.class){ event->
            deviceItems<<event.deviceItem
            //这里检测,当前是否有任务执行.如果有.则启动
            if(!currentPlan){
                Log.e(TAG,"设备:${event.deviceItem.toString()} 接入,但当前没有任务执行,等待任务!")
            } else {
                Log.e(TAG,"设备:${event.deviceItem.toString()} 开始准备执行任务:$currentPlan.name")
                executeDeviceAction(currentPlan,event.deviceItem)
            }
        }
        //设备断开
        RxBus.subscribe(OnDeviceDisConnectedEvent.class){ item->
            //中断设备执行
            deviceItems.remove(item.deviceItem)
            def testItem=runTestItems.find {it.deviceItem==item.deviceItem}
            if(testItem){
                //移除条目
                runTestItems.remove(testItem)
                //结束任务
                testItem.destroy()
            }
        }
        //手动启动设备任务事件
        RxBus.subscribe(OnDeviceStartEvent.class){ event->
            def findItem=runTestItems.find {it.deviceItem==event.deviceItem}
            if(findItem){
                Log.e(TAG,"设备:${event.deviceItem.toString()} 己运行!")
            } else if(currentPlan){
                //当前有任务正在运行
                executeDeviceAction(currentPlan,event.deviceItem)
            } else {
                //当前没有任务执行,检测执行
                initTestPlanItems(planItems)
            }
        }
        //手动结束设备运行事件
        RxBus.subscribe(OnDeviceStopEvent.class){ event->
            threadPool.execute({
                //结束当前任务
                final def deviceItem=event.deviceItem
                def findItem=runTestItems.find {it.deviceItem==deviceItem}
                if(findItem){
                    runTestItems.remove(findItem)
                    findItem.destroy()
                    def adb=SharedPrefs.get(PrefsKey.ADB)
                    def testCaseItem=findItem.testCaseItem
                    def result1=Command.exec("$adb -s $deviceItem.serialNumber shell am force-stop $testCaseItem.apkPackage")
                    def result2=Command.exec("$adb -s $deviceItem.serialNumber shell am force-stop $testCaseItem.testPackage")
                    Log.e(TAG,"设备:${deviceItem.toString()} 强行结束任务:${findItem.testPlanItem.name} 结果:$result1.exit $result2.exit")
                    RxBus.post(new OnActionStopEvent(deviceItem))
                }
            })
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
        //2:检测当前定时器,是否有定时任务正在等待,先移除等待任务,再整体处理任务
        if(null!=testPlanTimerTask&&!testPlanTimerTask.running){
            testPlanTimerTask.cancel()
            timer.purge()
        }
        //3:检测当前有无任务可以立即执行: 当前时间为12:00 9:00-13:00 cycle 11:-13:00 not cycle
        def runItems = findRunPlanItems()
        //4:有马上可以运行计划,且任务数大于2
        if(runTestItems){
            //5:有可执行条目,检测是否替换为优先级高的任务
            loopNewPlan(currentPlan)
            setRunNextTestPlanInfo(getNextTestPlanItem(currentPlan))
        } else {
            //当前没有可执行条目
            if (runItems) {
                //6:重新检测任务执行时间,若循环任务执行与下一个一性次任务间隔时间非常短,如2分钟,则放弃执行周期任务,因为一次性任务执行优先级更高
                //12:01  11:30-13:00 11:35-13:00 not cycle
                def runSize=runItems.size()
                if (1 == runSize) {
                    planItems.remove(runItems[0])
                    planItems.add(0,runItems[0])
                    Platform.runLater({ runTestPlan(runItems[0]) })
                } else if(2==runSize&&runItems[0].cycle^runItems[1].cycle){
                    //这里检测,1:如果任务1为cycle,而与任务2时间间隔很短,直接执行
                    //如果不是循环任务,直接执行
                    if(!runItems[0].cycle){
                        Platform.runLater({runTestPlan(runItems[0])})
                    } else if(!runItems[1].cycle){
                        Platform.runLater({runTestPlan(runItems[1])})
                    }
                } else {
                    // 异常情况,一般不可能出现,3个以上的并行任务.因为同一个同段,只可能存在循环与不循环任务相交
                    def dateTime=LocalDateTime.now()
                    File file=new File(FilePrefs.EXCEPTION_FOLDER,dateTime.toString()+".txt")
                    def out=new StringBuilder("Date:${dateTime.toString()}\n")
                    runItems.each { out.append("任务:$it.name 测试用例:$it.testCase 起始时间:$it.startDate 结束时间:$it.endDate 周期任务:$it.cycle\n") }
                    file.withWriter {it.write(out.toString()) }
                    Log.e(TAG,"出现3个并行执行任务,记录日志:$file.name 请检测!")
                }
            } else if (planItems) {
                //7:找下一个时间距离最近的任务,当前没有可执行任务,检测可循环任务
                Platform.runLater({ runTimerTestPlan(planItems[0],null) })
            } else {
                //没有任务可以执行了,重置信息
                setRunTestPlanInfo()
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
     * 查找当前时间点可以立即运行的条目
     * @return
     */
    def findRunPlanItems(){
        final long time=LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        planItems.findAll { getTestPlanTime(it,it.st)<time&&it.et>time }
    }

    /**
     * 查找在指定时间范围内的可执行任务
     * @param start
     * @param end
     * @return
     */
    def findRunPlanItems(long start,long end){
        planItems.findAll { TestPlanItem item->
            long startTime=getTestPlanTime(item,item.st)
            startTime>start&&startTime<end
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
    @Override
    void runTestPlan(TestPlanItem item,TestPlanItem lastItem=null) {
        if(lastItem){
            //结束上一个任务,因为有任务可能正在进行
            def testItems=runTestItems.findAll {it.testPlanItem==lastItem}
            if(testItems){
                //移除正在执行的任务.并将当前正在执行任务排到最前面.重新加载
                testItems.each {
                    runTestItems.remove(it)
                    it.actionService?.destroy()
                }
                planItems.remove(item)
                planItems.add(0,item)
                testPlanList.getItems().clear()
                testPlanList.getItems().addAll(planItems)
            }
        }
        if(item){
            currentPlan=item
            setRunTestPlanInfo(item)
            //检测用户任务运行状态
            if(!runTestItems){
                //当前不存在执行任务,直接执行
                executeAllDeviceAction(item)
            }
            //检测执行任务.检测任务
            loopNewPlan(item)
        }
        //下一个执行任务
        TestPlanItem nextItem=getNextTestPlanItem(item)
        if(!nextItem){
            //恢复默认
            nextTestPlan.setText("##")
            nextTestStartTime.setText("##")
            nextTestEndTime.setText("##")
        } else {
            nextTestPlan.setText(nextItem.name)
            def nowDate = LocalDate.now()
            long nowTimeMillis=LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            long endTime=item.et+new LocalDateTime(nowDate,LocalTime.of(0,0,0)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            if(endTime<nowTimeMillis){
                def newDate=nowDate.plusDays(1)
                nextTestStartTime.setText(newDate.toString()+" "+nextItem.startDate)
                nextTestEndTime.setText(newDate.toString()+" "+nextItem.endDate)
            } else {
                nextTestStartTime.setText(nowDate.toString()+" "+nextItem.startDate)
                nextTestEndTime.setText(nowDate.toString()+" "+nextItem.endDate)
            }
            nextTestPlan.setText(nextItem.name)
        }
    }

    void setRunTestPlanInfo(TestPlanItem item=null){
        testPlanName.setText(item?item.name:"暂无任务")
        testCaseName.setText(item?item.testCase:"##")
        testStartTime.setText(item?item.startDate:"##")
        testEndTime.setText(item?item.endDate:"##")
    }

    void setRunNextTestPlanInfo(TestPlanItem item=null){
        nextTestPlan.setText(item?item.name:"##")
        nextTestStartTime.setText(item?item.startDate:"##")
        nextTestEndTime.setText(item?item.endDate:"##")
    }


    /**
     * 初始化执行定时任务计划
     * @param item
     */
    def runTimerTestPlan(TestPlanItem item,TestPlanItem currentItem) {
        //待执行执行任务
        if(item){
            //重置执行任务提示
            currentItem?:setRunTestPlanInfo()

            nextTestPlan.setText(item.name)
            nextTestStartTime.setText(item.startDate)
            nextTestEndTime.setText(item.endDate)

            //启动定时器,准备在示来时刻执行
            if(null!=testPlanTimerTask){
                testPlanTimerTask.cancel()
                timer.purge()
            }
            //启动一个定时任务
            testPlanTimerTask=new TestPlanTimerTask(this,item,currentItem)
            long startTimeMillis=getTestPlanTime(item,item.st)
            def date=new Date(startTimeMillis)
            timer.schedule(testPlanTimerTask,date)
            if(currentItem){
                Log.i(TAG,"当前:${currentItem.name}正在执行,任务:${item.name}具有更高优先级,将在:$item.startDate 执行")
            } else {
                Log.i(TAG,"当前没有任务执行,为任务:$item.name 启动了一个定时任务:$item.startDate")
            }
        }
    }

    /**
     * 运行所有设备计划任务
     * @param item
     * @return
     */
    def executeAllDeviceAction(TestPlanItem item){
        def testCaseItem=DbHelper.helper.queryTestCase(item.caseId)
        if(!testCaseItem){
            //脚本记录不存在,可能为人为删除
            Log.e(TAG,"执行任务时,检测到用例:$item.testCase ID:$item.caseId 不存在!")
        } else {
            deviceItems.each {startInstallAction(it,item,testCaseItem)}
        }
    }

    /**
     * 运行单台设备计划任务
     * @param item
     * @return
     */
    def executeDeviceAction(TestPlanItem item,DeviceItem deviceItem){
        def testCaseItem=DbHelper.helper.queryTestCase(item.caseId)
        if(!testCaseItem){
            //脚本记录不存在,可能为人为删除
            Log.e(TAG,"执行任务时,检测到用例:$item.testCase ID:$item.caseId 不存在!")
        } else {
            startInstallAction(deviceItem, item, testCaseItem)
        }
    }

    /**
     * 开始启动安装计划任务
     * @param deviceItem
     * @param item
     * @param testCaseItem
     */
    void startInstallAction(DeviceItem deviceItem, TestPlanItem item, TestCaseItem testCaseItem) {
        final String sdkPath = SharedPrefs.get(PrefsKey.SDK)
        def installService = new InstallService(sdkPath, deviceItem, item, testCaseItem)
        installService.actionCallback{
            //启动用例任务
            if(What.INSTALL.TYPE_INSTALL_SUCCESS==it.type) {
                startTaskAction(sdkPath,deviceItem,item,testCaseItem)
            } else if(What.INSTALL.TYPE_USER_RESTRICTED==it.type){
                //用户主动拒绝程序安装,结束流程
                def address=deviceItem.getDeviceProperty(Property.DEVICE_ADDRESS)
                !address?: RxBus.post(new OnUserRestrictedEvent(address))
                Log.e(TAG,"设备:${deviceItem.toString()} 拒绝了程序安装,中止本机任务!")
                installService.destroy()
            }
        }
        //保存安装对象,安装成功后移除
        if(!runTestItems){
            def result=Command.exec("pkill bash")
            Log.e(TAG,"任务:$item.name 执行前,清理进程:$result.exit")
        }
        runTestItems << new RunTestItem(this,sdkPath,deviceItem, item,testCaseItem,installService)
        threadPool.execute(installService)
        //发送任务开始执行事件,由任围更新状态
        RxBus.post(new OnActionStartEvent(deviceItem))
    }

    /**
     * 开始启动计划任务
     * @param deviceItem
     * @param item
     * @param testCaseItem
     */
    void startTaskAction(String sdkPath,DeviceItem deviceItem,TestPlanItem item,TestCaseItem testCaseItem){
        //结束安装任务
        def runItem=runTestItems.find {it.deviceItem==deviceItem}
        if(runItem){
            //当前执行次数
            int runCount=runItem.runCount
            //取消本次任务内,安装服务,以及运行服务
            runItem.reset()
            //开始调试任务
            def actionService = new ActionService(sdkPath, deviceItem, item, testCaseItem,runCount)
            actionService.actionCallback { processMessage(deviceItem,item, it) }
            runItem.actionService=actionService
            threadPool.execute(actionService)
        }
    }


    /**
     * 处理安装消息
     * @param message
     */
    def processMessage(DeviceItem deviceItem,TestPlanItem testPlanItem,item){
        //记录信息
        switch (item.type){
            case What.TASK.TYPE_LOG:
                Log.i(TAG,item.message)
                break
            case What.TASK.TYPE_RUN_LOOP:
                //检测任务是否正常运行,记录此次脚本执行时间,如果两次时间一次执行后,在一段时间里,没有执行.说明测试进程可能出现问题,需要重启
                def runItem=runTestItems.find {it.deviceItem==deviceItem}
                if(!runItem){
                    Log.e(TAG,"设备:${deviceItem.toString()} 任务轮询时,发现运行条目不存在!")
                } else {
                    runItem.checkTask.addActionTime()//添加此次运行时长
                    runItem.checkTask.resetTask()//重置检测任务
                }
                break
            case What.TASK.TYPE_RUN_RESULT:
                //获取执行结果
                break
            case What.TASK.TYPE_RUN_COMPLETE:
                println "$testPlanItem.name 执行完毕!"
                //移除检测任务
                def runItem=runTestItems.find {it.deviceItem==deviceItem}
                if(runItem) {
                    runItem.destroy()
                    //移除当前条目
                    runTestItems.remove(runItem)
                    if(!runTestItems){
                        //杀死所有bash进程
                        def result=Command.exec("pkill bash")
                        Log.i(TAG,"任务:${testPlanItem.name} 清理所有子进程:$result.exit!")
                        Platform.runLater({
                            currentPlan=null
                            //2:移除己执行条目
                            testPlanList.getItems().remove(testPlanItem)
                            //3:重新检测所有任务
                            initTestPlanItems(planItems-testPlanItem)
                            //4:任务己执行完,通知其他界面
                            RxBus.post(new OnTestPlanRunCompleteEvent(testPlanItem))
                        })
                    }
                }
                break;
        }
    }
/**
     * 遍历新的可执行计划
     * @return
     */
    def loopNewPlan(TestPlanItem item){
        def newPlan=queryTestPlanItems()
        if(newPlan){
            long startTime=getTestPlanTime(newPlan,newPlan.st)
            long nowTime=new LocalDateTime(LocalDate.now(),LocalTime.now()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            //非循环任务,不允许插入
            if (!newPlan.cycle) {
                if(startTime<nowTime){
                    //小于一个时间间隔,立即执行,并结束上一个任务
                    //复置newPlan在集合的顺序,因为其他任务可能在他之前,但他却先执行,导致的混乱
                    runTestPlan(newPlan,item)
                } else {
                    //定时执行
                    Platform.runLater({ runTimerTestPlan(newPlan,currentPlan) })
                }
            }
        }
    }

    /**
     * 检测当前测试计划是否有变动,如中间插入一条记录,需要优先执行
     */
    def queryTestPlanItems(){
        def newPlan
        //循环任务优先级低于一次性任务,检测是否有任务可以设置中途插入
        if(planItems&&currentPlan&&currentPlan.cycle){
            //获得当前任务结束时间
            long startTime=getTestPlanTime(currentPlan,currentPlan.st)
            def runPlanItems=findRunPlanItems(startTime,currentPlan.et)
            if(runPlanItems){
                runPlanItems-=currentPlan
                if(runPlanItems){
                    //仍然有任务可以执行,检测其是否可以立即执行,否则定时执行
                    def sortPlanItems=sortTestPlanItems(runPlanItems)
                    newPlan=sortPlanItems[0]
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
