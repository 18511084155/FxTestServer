package quant.test.server.controller
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXCheckBox
import com.jfoenix.controls.JFXSnackbar
import com.jfoenix.controls.JFXTextField
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.fxml.FXML
import javafx.scene.control.DatePicker
import javafx.scene.layout.StackPane
import quant.test.server.StageManager
import quant.test.server.callback.InitializableArgs
import quant.test.server.model.TestPlanItem
import quant.test.server.widget.TimeSpinner

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
    @FXML JFXCheckBox cycleCheckBox
    @FXML JFXButton cancelButton
    @FXML JFXButton applyButton
    List<TestPlanItem> items

    def lastTime1,lastTime2

    @Override
    void setArgs(List<TestPlanItem> items) {
        this.items=items
    }

    @Override
    void initialize(URL location, ResourceBundle resources) {
        //2:检索出所有记录,做任务时间过滤
        //1:制定输入起始时间
        //2:制定输入结束时间
        //文字变化监听
        snackBar.registerSnackbarContainer(root)
        startTimeSpinner.editor.textProperty().addListener({ObservableValue<? extends String> observable, String oldValue, String newValue->
            println "oldValue:$oldValue newValue:$newValue"
        } as ChangeListener<String>)

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
            def localTime= LocalTime.now().plusHours(2)//添加2小时,默认
            endTimeSpinner.valueFactory.setValue(LocalTime.of(localTime.hour,localTime.minute,localTime.second))
        } else {
            //有记录,过滤记录时间,取最大的时间,往后推
            TestPlanItem endItem
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
                localTime= LocalTime.of(calendar.get(Calendar.HOUR),calendar.get(Calendar.MINUTE),calendar.get(Calendar.SECOND))
            }
            //开始时间
            startTimeSpinner.valueFactory.setValue(localTime)
            //结束时间
            endTimeSpinner.valueFactory.setValue(localTime.plusHours(2))
        }

        cancelButton.setOnMouseClicked({StageManager.instance.getStage(this)?.hide()})
        applyButton.setOnMouseClicked({addTestPlan()})

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

    def value2TimeMillis(date,value){
        //获得天时间
        def dayMs=value2TimeMillis(value)
        //获得日期时间

    }


    def validatorDate(){

    }


    /**
     * 添加任务计划
     */
    def addTestPlan() {

    }

}
