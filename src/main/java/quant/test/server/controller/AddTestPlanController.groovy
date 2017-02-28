package quant.test.server.controller
import com.jfoenix.controls.*
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.fxml.FXML
import javafx.scene.control.DateCell
import javafx.scene.control.DatePicker
import javafx.scene.layout.StackPane
import javafx.util.Callback
import quant.test.server.StageManager
import quant.test.server.callback.InitializableArgs
import quant.test.server.database.DbHelper
import quant.test.server.model.TestCaseItem
import quant.test.server.model.TestPlanItem
import quant.test.server.widget.TimeSpinner
import quant.test.server.widget.datepicker.DateCellItem
import rx.Observable
import rx.schedulers.Schedulers

import java.time.LocalDate
import java.time.LocalTime
/**
 * Created by cz on 2017/2/23.
 */
class AddTestPlanController implements InitializableArgs<List<TestPlanItem>>{
    @FXML StackPane root
    @FXML JFXSnackbar snackBar
    @FXML JFXTextField testPlanName
    @FXML TimeSpinner startTimeSpinner
    @FXML DatePicker startDatePicker
    @FXML TimeSpinner endTimeSpinner
    @FXML DatePicker endDatePicker
    @FXML JFXComboBox<TestCaseItem> jfxComboBox
    @FXML JFXCheckBox cycleCheckBox
    @FXML JFXButton cancelButton
    @FXML JFXButton applyButton
    @FXML JFXDialog dialog
    @FXML JFXButton acceptButton
    List<TestPlanItem> items
    def lastTime1,lastTime2
    TestPlanItem endItem

    @Override
    void setArgs(List<TestPlanItem> items) {
        this.items=items
    }

    @Override
    void initialize(URL location, ResourceBundle resources) {
        //初始化警告dialog
        initAlertDialog()
        //2:检索出所有记录,做任务时间过滤
        //1:制定输入起始时间
        //2:制定输入结束时间
        //文字变化监听
        snackBar.registerSnackbarContainer(root)
        startTimeSpinner.editor.textProperty().addListener(new ChangeListener<String>() {
            @Override
            void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                //检测时间变化
                //3:如果是循环执行任务,必须不在循环时间段内
                def timeMillis=startTimeSpinner.timeMillis
                if(-1<timeMillis){
                    if(cycleCheckBox.isSelected()){
                        //如果为循环时间
                        //1:必须小于结束时间
                        if(timeMillis>=endTimeSpinner.timeMillis){
                            //提示选择异常
                            Platform.runLater({
                                startTimeSpinner.setEditError(true)
                                snackBar.fireEvent(new JFXSnackbar.SnackbarEvent("选择时间段必须小于结束时间!",null,3000, null))
                            })
                        } else if(checkCycleTimeRange(items,timeMillis)){
                            //2:必须在己存在计划范围外
                            Platform.runLater({
                                startTimeSpinner.setEditError(true)
                                snackBar.fireEvent(new JFXSnackbar.SnackbarEvent("选择时间段重复,请查看己存在计划,并重新选择!",null,3000, null))
                            })
                        } else {
                            startTimeSpinner.setEditError(false)
                        }
                    } else {
                        //如果不循环
                    }
                }
            }

        })

        startTimeSpinner.editor.focusedProperty().addListener({ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue->
            if(newValue){
                //获取焦点,记录原始时间
                lastTime1=startTimeSpinner.editor.text
                println "get:$startTimeSpinner.editor.text"
            } else {
                //失去焦点,判断时间是否符合
                value2TimeMillis(startTimeSpinner.editor.text)
            }
        } as ChangeListener<Boolean>)

        startDatePicker.setOnAction({
            LocalDate date = startDatePicker.getValue();
            println "Selected date: " + date
            startDatePicker.show();
            startDatePicker.requestFocus();
        });


        //选中循环监听
        cycleCheckBox.selectedProperty().addListener({ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue->
            startDatePicker.setDisable(newValue)
            endDatePicker.setDisable(newValue)
        } as ChangeListener<Boolean>)


        if(!items){
            //没有任何记录
            //添加2小时,默认
            LocalTime nowTime=LocalTime.now()
            startTimeSpinner.valueFactory.setValue(nowTime)
            endTimeSpinner.valueFactory.setValue(nowTime.plusHours(2))
            startDatePicker.setValue(LocalDate.now())
            endDatePicker.setValue(LocalDate.now())
        } else {
            //有记录,过滤记录时间,取最大的时间,往后推
            items.each { !endItem&&endItem.et>=it.et?:(endItem=it) }

            //操作时间
            LocalTime localTime
            if(endItem.cycle){
                //循环时间节点
                localTime=LocalTime.now()
                endItem.et<localTime.toSecondOfDay()?:(localTime=LocalTime.ofSecondOfDay(endItem.et))
            } else {
                Calendar calendar=Calendar.instance
                calendar.setTimeInMillis(endItem.et)
                startDatePicker.setValue(new LocalDate(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH)+1,calendar.get(Calendar.DAY_OF_MONTH)))
                endDatePicker.setValue(new LocalDate(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH)+1,calendar.get(Calendar.DAY_OF_MONTH)))
                localTime= LocalTime.of(calendar.get(Calendar.HOUR),calendar.get(Calendar.MINUTE),calendar.get(Calendar.SECOND))
            }
            //开始时间
            startTimeSpinner.valueFactory.setValue(localTime)
            //结束时间
            endTimeSpinner.valueFactory.setValue(localTime.plusHours(2))
        }

        startDatePicker.setDayCellFactory({
            new DateCellItem(LocalDate.now())
        } as Callback<DatePicker, DateCell>)

        cancelButton.setOnMouseClicked({StageManager.instance.getStage(this)?.hide()})
        applyButton.setOnMouseClicked({ addTestPlan() })

    }

    /**
     * 初始化警告对话框
     */
    private void initAlertDialog() {
        root.getChildren().remove(dialog)
        dialog.setTransitionType(JFXDialog.DialogTransition.CENTER);
        acceptButton.setOnMouseClicked({ StageManager.instance.getStage(this)?.hide() })
        //查询用户所有测试用例列,没有测试用例,无法添加
        Observable.create({DbHelper.helper.queryTestCase() } as Observable.OnSubscribe).
                observeOn(Schedulers.io()).subscribe({
                Platform.runLater({
                    if (!it) {
                        dialog.setOverlayClose(false)
                        dialog.show(root)
                    } else {
                        jfxComboBox.getItems().addAll(it)
                    }
                })
            },{it.printStackTrace()})
    }

    /**
     * 检测循环时间段
     * @param testPlanItems
     * @param l
     */
    def checkCycleTimeRange(List<TestPlanItem> items, long seconds) {
        boolean result=false
        items?.each {
            if(!it.cycle&&it.st<=seconds&&it.et>=seconds){
                result=true
                return
            }
        }
        result
    }


    /**
     * 时间格式化串格式化为毫秒值
     * @param value
     */
    def value2TimeMillis(value){
        int intValue=-1
        def matcher=value=~/(\d{1,2}):(\d{1,2}):(\d{1,2})/
        if(matcher) {
            def localTime = LocalTime.of(Integer.parseInt(matcher[0][1]), Integer.parseInt(matcher[0][2]), Integer.parseInt(matcher[0][3]))
            intValue=localTime.toSecondOfDay()
        }
        intValue
    }



    /**
     * 添加任务计划
     */
    def addTestPlan() {
        //添加任务
        if(cycleCheckBox.isSelected()){
            if(!testPlanName.text){
                snackBar.fireEvent(new JFXSnackbar.SnackbarEvent("请输入计划名称!",null,3000, null))
            } else if(startTimeSpinner.timeMillis>=endTimeSpinner.timeMillis){
                //提示选择异常
                Platform.runLater({ snackBar.fireEvent(new JFXSnackbar.SnackbarEvent("选择时间段必须小于结束时间!",null,3000, null)) })
            } else if(checkCycleTimeRange(items,startTimeSpinner.timeMillis)){
                //2:必须在己存在计划范围外
                Platform.runLater({ snackBar.fireEvent(new JFXSnackbar.SnackbarEvent("选择时间段重复,请查看己存在计划,并重新选择!",null,3000, null)) })
            } else {
                //任务可以添加,生成对象,然后添加

                DbHelper.helper.insertTestPlan()

            }
        }
    }

}
