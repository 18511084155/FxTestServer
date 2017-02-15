package quant.test.server.controller

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import quant.test.server.model.DeviceItem
import quant.test.server.model.Property

/**
 * Created by Administrator on 2017/2/15.
 */
class DeviceItemController implements Initializable{
    @FXML
    HBox root
    @FXML
    ImageView imageView
    @FXML
    Label deviceName
    @FXML
    Label socketStatus
    @FXML
    Circle runStatus


    Pane getView(){
        return root
    }

    @Override
    void initialize(URL location, ResourceBundle resources) {
    }

    def setDeviceItem(DeviceItem deviceItem) {
        imageView.setImage(new Image(getClass().getResourceAsStream(deviceItem.isUsbConnect()?"image/ic_settings_input_hdmi.png":"image/ic_settings_input_antenna.png")))
        deviceName.setText("$deviceItem.brand-$deviceItem.model")
        socketStatus.setText(deviceItem.getProperty(Property.DHCP_WLAN0_IPADDRESS))
        runStatus.setFill(new Color(1.0,1.0,1.0,1.0))
    }
}
