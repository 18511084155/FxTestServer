package quant.test.server.controller

import com.jfoenix.controls.JFXTreeTableView
import javafx.fxml.FXML
import javafx.fxml.Initializable
import quant.test.server.anntation.FXMLLayout

/**
 * Created by Administrator on 2017/2/19.
 */
@FXMLLayout("fxml/test_doc_layout.fxml")
class TestDocController implements Initializable{
    @FXML JFXTreeTableView treeTableView
    @Override
    void initialize(URL location, ResourceBundle resources) {

    }
}
