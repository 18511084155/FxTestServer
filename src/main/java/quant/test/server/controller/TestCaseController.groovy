package quant.test.server.controller

import com.jfoenix.controls.JFXButton
import javafx.fxml.FXML
import javafx.fxml.Initializable
import quant.test.server.StageManager
import quant.test.server.anntation.FXMLLayout

/**
 * Created by Administrator on 2017/2/19.
 */
@FXMLLayout("fxml/testcase_layout.fxml")
class TestCaseController implements Initializable{
    @FXML JFXButton addTaskCase
    @Override
    void initialize(URL location, ResourceBundle resources) {
        addTaskCase.setOnMouseClicked({StageManager.instance.newStage(getClass().getClassLoader().getResource("fxml/add_task_case_layout.fxml"),720,640)?.show()})
    }
}
