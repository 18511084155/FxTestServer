package quant.test.server.controller

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import quant.test.server.model.TestPlanItem

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Created by cz on 2017/3/6.
 */
class TestPlanItemController {
    @FXML HBox root
    @FXML Rectangle topRect
    @FXML Rectangle bottomRect
    @FXML Circle innerCircle
    @FXML Circle outerCircle

    @FXML Label testPlanName
    @FXML Label testCaseName
    @FXML Label testStartTime
    @FXML Label testEndTime
    @FXML Label planCycle


    TestPlanItemController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/test_plan_item.fxml"))
        fxmlLoader.setController(this)
        fxmlLoader.load()
    }

    Pane getView(){
        return root
    }

    def bindItem(TestPlanItem item) {
        root.setDisable(true)
        testPlanName.setText(item.name)
        testCaseName.setText(item.testCase)
        if(item.cycle){
            def nowDate = LocalDate.now()
            long nowTimeMillis=LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            long endTime=item.et+new LocalDateTime(nowDate,LocalTime.of(0,0,0)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            if(endTime<nowTimeMillis){
                def newDate=nowDate.plusDays(1)
                testStartTime.setText(newDate.toString()+" "+item.startDate)
                testEndTime.setText(newDate.toString()+" "+item.endDate)
            } else {
                testStartTime.setText(nowDate.toString()+" "+item.startDate)
                testEndTime.setText(nowDate.toString()+" "+item.endDate)
            }
        } else {
            testStartTime.setText(item.startDate)
            testEndTime.setText(item.endDate)
        }
        planCycle.setText(String.valueOf(item.cycle))
    }

}
