package quant.test.server.controller
import de.jensd.fx.fontawesome.Icon
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TextArea
import quant.test.server.anntation.FXMLLayout
import quant.test.server.log.Log

import javax.annotation.PreDestroy
/**
 * Created by cz on 2017/2/17.
 */
@FXMLLayout("fxml/message_layout.fxml")
class MessageController implements Initializable,Observer{
    @FXML TextArea messageArea
    @FXML Icon buttonTrash
    @Override
    void initialize(URL location, ResourceBundle resources) {
        Log.registerObservable(this)
        println "MessageController-initialize"
        buttonTrash.setOnMouseClicked({ Log.e("test","test message!")})
    }

    @PreDestroy
    void onDestroy(){
        Log.unregisterObservable(this)
    }

    @Override
    void update(Observable o, Object arg) {
        if(Platform.fxApplicationThread){
            messageArea.appendText(arg.toString()+"\n")
        } else {
            Platform.runLater({messageArea.appendText(arg.toString()+"\n")})
        }
    }
}
