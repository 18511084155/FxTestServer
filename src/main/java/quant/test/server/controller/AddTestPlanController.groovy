package quant.test.server.controller

import com.android.ddmlib.Log
import com.jfoenix.controls.*
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.control.ComboBox
import javafx.scene.control.DateCell
import javafx.scene.control.DatePicker
import javafx.scene.control.TreeItem
import javafx.scene.layout.StackPane
import javafx.util.Callback
import quant.test.server.StageManager
import quant.test.server.bus.RxBus
import quant.test.server.callback.InitializableArgs
import quant.test.server.database.DbHelper
import quant.test.server.event.OnTestPlanAddedEvent
import quant.test.server.model.TestCaseItem
import quant.test.server.model.TestPlanItem
import quant.test.server.model.TestPlanProperty
import quant.test.server.scheduler.MainThreadSchedulers
import quant.test.server.widget.MyJFXSnackbar
import quant.test.server.widget.TimeSpinner
import quant.test.server.widget.datepicker.DateCellItem
import quant.test.server.widget.datepicker.MyDatePicker
import rx.Observable
import rx.schedulers.Schedulers

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
/**
 * Created by cz on 2017/2/23.
 */
class AddTestPlanController implements InitializableArgs<List<TestPlanItem>>,ChangeListener<String>{
    final def TAG="AddTestPlanController"
    @FXML StackPane root
    @FXML MyJFXSnackbar snackBar
    @FXML JFXTextField testPlanName
    @FXML TimeSpinner startTimeSpinner
    @FXML MyDatePicker startDatePicker
    @FXML TimeSpinner endTimeSpinner
    @FXML MyDatePicker endDatePicker
    @FXML ComboBox<TestCaseItem> comboBox
    @FXML JFXCheckBox cycleCheckBox
    @FXML JFXButton cancelButton
    @FXML JFXButton applyButton
    @FXML JFXDialog dialog
    @FXML JFXButton acceptButton

    @FXML JFXTreeTableView treeTableView
    @FXML JFXTreeTableColumn testPlanColumn
    @FXML JFXTreeTableColumn startTimeColumn
    @FXML JFXTreeTableColumn endTimeColumn

    List<TestPlanItem> items
    def TestCaseItem selectCaseItem


    @Override
    void initializeWithArgs(List<TestPlanItem> items) {
        this.items=items
        snackBar.registerSnackbarContainer(root)
        //初始化警告dialog
        initAlertDialog()
        //2:检索出所有记录,做任务时间过滤
        //1:制定输入起始时间
        //2:制定输入结束时间
        //文字变化监听
        initDateAction()

        //初始化起始的本地时间
        initLocalTime(items)

        comboBox.valueProperty().addListener({ObservableValue observable, TestCaseItem oldValue, TestCaseItem newValue->
            this.selectCaseItem=newValue
        } as ChangeListener<TestCaseItem>)

        cancelButton.setOnMouseClicked({StageManager.instance.getStage(this)?.hide()})
        applyButton.setOnMouseClicked({ addTestPlan() })

    }
    /**
     * 初始化起始本地时间
     * @param items
     */
    private void initLocalTime(List<TestPlanItem> items) {
        if (!items) {
            //没有任何记录
            //添加2小时,默认
            LocalTime nowTime = LocalTime.now()
            startTimeSpinner.valueFactory.setValue(nowTime)
            endTimeSpinner.valueFactory.setValue(nowTime.plusHours(2))
            //操作时间
            startDatePicker.setValue(LocalDate.now())
            endDatePicker.setValue(LocalDate.now())
        } else {
            //初始化测试计划表格
            initTestPlan(items)
            //有记录,过滤记录时间,取最大的时间,往后推
            refreshLocalTime()
        }

    }

    @Override
    void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        //检测时间变化
        if(cycleCheckBox.isSelected()){
            notifyCycleTimeTextChanged()
        } else {
            notifyTimeTextChanged()
        }
    }

    /**
     * 刷新当前条目状态信息
     */
    private void refreshLocalTime() {
        //设定起始一次性任务时间
        startTimeSpinner.editor.textProperty().removeListener(this)
        endTimeSpinner.editor.textProperty().removeListener(this)
        if(cycleCheckBox.isSelected()){
            TestPlanItem endCycleItem
            items.each { !it.cycle || (endCycleItem && endCycleItem.et >= it.et) ?: (endCycleItem = it) }
            //设定循环任务起始时间

            LocalTime localTime = LocalTime.now()
            if (endCycleItem&&endCycleItem.et >= localTime.toSecondOfDay()) {
                localTime=getTimeSecond(endCycleItem.endDate)
            }
            localTime=localTime.plusSeconds(1)
            startTimeSpinner.valueFactory.setValue(localTime)
            endTimeSpinner.valueFactory.setValue(localTime.plusHours(2))
            startDatePicker.setValue(LocalDate.now())
            endDatePicker.setValue(LocalDate.now())
        } else {
            TestPlanItem endItem
            items.each { it.cycle || (endItem && endItem.et >= it.et) ?: (endItem = it) }

            LocalDateTime localDateTime=LocalDateTime.now()
            def endMillis=localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            if (endItem&&endItem.et > endMillis) {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"))
                calendar.setTimeInMillis(endItem.et)

                def localDate=new LocalDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
                def localTime=new LocalTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND)+1,0)
                localDateTime=new LocalDateTime(localDate,localTime)
                startTimeSpinner.valueFactory.setValue(localTime)
                endTimeSpinner.valueFactory.setValue(localTime.plusHours(2))

                startDatePicker.setValue(localDate)
                endDatePicker.setValue(localDate)
            } else {
                final def localDate=localDateTime.toLocalDate()
                def localTime = localDateTime.toLocalTime().plusSeconds(1)
                startTimeSpinner.valueFactory.setValue(localTime)
                endTimeSpinner.valueFactory.setValue(localTime.plusHours(2))
                startDatePicker.setValue(localDate)
                endDatePicker.setValue(localDate)

            }
            startDatePicker.setDayCellFactory({ new DateCellItem(localDateTime.toLocalDate()) } as Callback<DatePicker, DateCell>)
            endDatePicker.setDayCellFactory({ new DateCellItem(localDateTime.toLocalDate()) } as Callback<DatePicker, DateCell>)
        }
        startTimeSpinner.editor.textProperty().addListener(this)
        endTimeSpinner.editor.textProperty().addListener(this)
    }

    /**
     * 初始化日期事件
     * @param items
     */
    private void initDateAction() {
        startTimeSpinner.editor.textProperty().addListener(this)
        endTimeSpinner.editor.textProperty().addListener(this)
        startDatePicker.setOnAction({
            notifyTimeTextChanged()
            startDatePicker.show();
            startDatePicker.requestFocus();
        });

        //选中循环监听
        cycleCheckBox.selectedProperty().addListener({ ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue ->
            //更新计划
            initTestPlan(items)
            //更新当前本地时间显示
            refreshLocalTime()
            //根据循环任务显示日历
            startDatePicker.setDisable(newValue)
            endDatePicker.setDisable(newValue)
            if(newValue){
                notifyCycleTimeTextChanged()
            } else {
                //不循环的执行任务
                notifyTimeTextChanged()
            }
        } as ChangeListener<Boolean>)
    }

    /**
     * 通知循环时间变化检测
     */
    private void notifyCycleTimeTextChanged() {
        def startTimeSecond = startTimeSpinner.timeMillis
        def endTimeSecond = endTimeSpinner.timeMillis
        if (-1 < startTimeSecond) {
            //如果为循环时间
            //1:必须小于结束时间
            int index = items.findIndexOf { it.cycle && it.st <= startTimeSecond && it.et >= startTimeSecond }
            if (startTimeSecond >= endTimeSpinner.timeMillis) {
                //提示选择异常
                Platform.runLater({
                    startTimeSpinner.setEditError(true)
                    snackBar.fireEvent(new JFXSnackbar.SnackbarEvent("选择时间段必须小于结束时间!", null, 2000, null))
                })
            } else if (-1 < index) {
                //2:必须在己存在计划范围外
                Platform.runLater({
                    startTimeSpinner.setEditError(true)
                    treeTableView.selectionModel.select(index)
                    snackBar.fireEvent(new JFXSnackbar.SnackbarEvent("选择时间段重复,请查看己存在计划,并重新选择!", null, 2000, null))
                })
            } else if (items.find { it.cycle && startTimeSecond <= it.st && endTimeSecond >= it.et }) {
                //3:所选时间范围,必须在所有计划外
                Platform.runLater({
                    startTimeSpinner.setEditError(true)
                    endTimeSpinner.setEditError(true)
                    snackBar.fireEvent(new JFXSnackbar.SnackbarEvent("选择时间己包含其他计划,请重新选择!", null, 2000, null))
                })
            } else {
                startTimeSpinner.setEditError(false)
                endTimeSpinner.setEditError(false)
                snackBar.popupContainer
            }
        }
        startDatePicker.setEditError(false)
        endDatePicker.setEditError(false)
    }

    /**
     * 通知时间变化检测
     */
    private void notifyTimeTextChanged() {
        def startSecond=startTimeSpinner.timeMillis
        def endSecond=endTimeSpinner.timeMillis
        if (-1 < startSecond&&-1<endSecond&&startDatePicker.value&&endDatePicker.value) {
            def nowDateTime=LocalDateTime.now()
            LocalDateTime startDate=new LocalDateTime(startDatePicker.value,startTimeSpinner.localTime)
            LocalDateTime endDate=new LocalDateTime(endDatePicker.value,endTimeSpinner.localTime)
            def startMillis=startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            def endMillis=endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            def nowDateTimeMillis=nowDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            //1:必须小于结束时间
            int index = items.findIndexOf { !it.cycle && it.st <= startMillis && it.et >= startMillis }
            if(startMillis<nowDateTimeMillis){
                Platform.runLater({
                    startTimeSpinner.setEditError(true)
                    startDatePicker.setEditError(true)
                    snackBar.fireEvent(new JFXSnackbar.SnackbarEvent("常规任务无法选择比当前时间小的日期!", null, 2000, null))
                })
            } else if (startMillis >= endMillis) {
                //提示选择异常
                Platform.runLater({
                    startTimeSpinner.setEditError(true)
                    startDatePicker.setEditError(true)
                    snackBar.fireEvent(new JFXSnackbar.SnackbarEvent("选择时间段必须小于结束时间!", null, 2000, null))
                })
            } else if (-1 < index) {
                //2:必须在己存在计划范围外
                Platform.runLater({
                    startTimeSpinner.setEditError(true)
                    startDatePicker.setEditError(true)
                    treeTableView.selectionModel.select(index)
                    snackBar.fireEvent(new JFXSnackbar.SnackbarEvent("选择时间段重复,请查看己存在计划,并重新选择!", null, 2000, null))
                })
            } else if (items.find { !it.cycle && startMillis <= it.st && endMillis >= it.et }) {
                //3:所选时间范围,必须在所有计划外
                Platform.runLater({
                    startTimeSpinner.setEditError(true)
                    endTimeSpinner.setEditError(true)
                    startDatePicker.setEditError(true)
                    endDatePicker.setEditError(true)
                    snackBar.fireEvent(new JFXSnackbar.SnackbarEvent("选择时间己包含其他计划,请重新选择!", null, 2000, null))
                })
            } else {
                startTimeSpinner.setEditError(false)
                endTimeSpinner.setEditError(false)
                startDatePicker.setEditError(false)
                endDatePicker.setEditError(false)
            }
        }
    }

/**
     * 初始化测试计算表
     * @param testPlanItems
     */
    def initTestPlan(List<TestPlanItem> items) {
        if(items){
            def root=treeTableView.root
            def filterItems=items.findAll {cycleCheckBox.isSelected()?it.cycle:!it.cycle}
            if(root){
                //清空所有,再添加新的
                def treeItems=[]
                root.children.clear()
                filterItems?.each {treeItems<<new TreeItem(new TestPlanProperty(it))}
                !treeItems?:root.children.addAll(treeItems)
            } else {
                testPlanColumn.setCellValueFactory({ testPlanColumn.validateValue(it)?it.value.value.name: testPlanColumn.getComputedValue(it) })
                startTimeColumn.setCellValueFactory({ startTimeColumn.validateValue(it)? it.value.value.startDate: startTimeColumn.getComputedValue(it) })
                endTimeColumn.setCellValueFactory({ endTimeColumn.validateValue(it)? it.value.value.endDate: endTimeColumn.getComputedValue(it) })
                ObservableList<TestPlanProperty> testPlanItems = FXCollections.observableArrayList();
                filterItems.each {testPlanItems.add(new TestPlanProperty(it))}
                treeTableView.setRoot(new RecursiveTreeItem<TestPlanProperty>(testPlanItems, { it.getChildren() }))
                treeTableView.setShowRoot(false);
            }

        }
    }

    /**
     * 初始化警告对话框
     */
    private void initAlertDialog() {
        root.getChildren().remove(dialog)
        dialog.setTransitionType(JFXDialog.DialogTransition.CENTER);
        acceptButton.setOnMouseClicked({ StageManager.instance.getStage(this)?.hide() })
        //查询用户所有测试用例列,没有测试用例,无法添加
        Observable.create({
            def items=DbHelper.helper.queryTestCase()
            !items?:it.onNext(items)
            it.onCompleted()
        } as Observable.OnSubscribe).subscribeOn(Schedulers.io()).
                observeOn(MainThreadSchedulers.mainThread()).subscribe({ items->
                    if (!items) {
                        dialog.setOverlayClose(false)
                        dialog.show(root)
                    } else {
                        comboBox.getItems().addAll(items)
                        comboBox.getSelectionModel().selectFirst()
                        selectCaseItem=items[0]
                    }
            },{it.printStackTrace()})
    }


    /**
     * 检验测试信息
     */
    private boolean validatorTestInfo() {
        boolean result = false
        def startSecond = startTimeSpinner.timeMillis
        def endSecond = endTimeSpinner.timeMillis
        if (cycleCheckBox.isSelected()) {
            if (!testPlanName.text) {
                snackBar.fireEvent(new JFXSnackbar.SnackbarEvent("请输入计划名称!", null, 3000, null))
            } else if (startTimeSpinner.timeMillis >= endTimeSpinner.timeMillis) {
                //提示选择异常
                Platform.runLater({ snackBar.fireEvent(new JFXSnackbar.SnackbarEvent("选择时间段必须小于结束时间!", null, 3000, null)) })
            } else if (-1 < items.findIndexOf { !it.cycle && it.st <= startSecond && it.et >= endSecond }) {
                //2:必须在己存在计划范围外
                Platform.runLater({ snackBar.fireEvent(new JFXSnackbar.SnackbarEvent("选择时间段重复,请查看己存在计划,并重新选择!", null, 3000, null)) })
            } else {
                result = true
            }
        } else {
            LocalDateTime startDate=new LocalDateTime(startDatePicker.value,startTimeSpinner.localTime)
            LocalDateTime endDate=new LocalDateTime(endDatePicker.value,endTimeSpinner.localTime)
            def startMillis=startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            def endMillis=endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            if (!testPlanName.text) {
                snackBar.fireEvent(new JFXSnackbar.SnackbarEvent("请输入计划名称!", null, 3000, null))
            } else if (startMillis >= endMillis) {
                //提示选择异常
                Platform.runLater({ snackBar.fireEvent(new JFXSnackbar.SnackbarEvent("选择时间段必须小于结束时间!", null, 3000, null)) })
            } else if (-1 < items.findIndexOf { !it.cycle && it.st <= startMillis && it.et >= endMillis }) {
                //2:必须在己存在计划范围外
                Platform.runLater({ snackBar.fireEvent(new JFXSnackbar.SnackbarEvent("选择时间段重复,请查看己存在计划,并重新选择!", null, 3000, null)) })
            } else {
                result = true
            }
        }
        result
    }


    def getTimeSecond(value){
        LocalTime localTime
        def matcher=value=~/(\d{1,2}):(\d{1,2}):(\d{1,2})/
        if(matcher) {
            def hour=Integer.parseInt(matcher[0][1])
            def minute=Integer.parseInt(matcher[0][2])
            def second=Integer.parseInt(matcher[0][3])
            localTime = LocalTime.of(hour, minute, second)
        }
        localTime
    }

    /**
     * 添加任务计划
     */
    def addTestPlan() {
        //任务可以添加,生成对象,然后添加
        if(validatorTestInfo()){
            TestPlanItem planItem=new TestPlanItem()
            planItem.name=testPlanName.text
            planItem.caseId=selectCaseItem.id
            planItem.testCase=selectCaseItem.name

            if(cycleCheckBox.isSelected()){
                planItem.st=startTimeSpinner.timeMillis
                planItem.et=endTimeSpinner.timeMillis
                planItem.startDate=startTimeSpinner.text
                planItem.endDate=endTimeSpinner.text
            } else {
                LocalDateTime startDate=new LocalDateTime(startDatePicker.value,startTimeSpinner.localTime)
                LocalDateTime endDate=new LocalDateTime(endDatePicker.value,endTimeSpinner.localTime)
                planItem.st=startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                planItem.et=endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                planItem.startDate=startDate.toLocalDate().toString()+" "+startDate.toLocalTime().toString()
                planItem.endDate=endDate.toLocalDate().toString()+" "+endDate.toLocalTime().toString()
            }
            planItem.cycle=cycleCheckBox.isSelected()
            DbHelper.helper.insertTestPlan(planItem)
            Log.e(TAG,"用户:$planItem.uid 添加任务计划:$planItem.name $planItem.startDate-$planItem.endDate 成功!")
            RxBus.post(new OnTestPlanAddedEvent(planItem))
            StageManager.instance.getStage(this)?.hide()
        }
    }

}
