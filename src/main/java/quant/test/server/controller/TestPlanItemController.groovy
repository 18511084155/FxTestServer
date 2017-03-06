package quant.test.server.controller

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import quant.test.server.model.TestPlanItem
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
        testStartTime.setText(item.startDate)
        testEndTime.setText(item.endDate)
        planCycle.setText(String.valueOf(item.cycle))
    }

}
