package quant.test.server.controller

import com.jfoenix.effects.JFXDepthManager
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
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
class DeviceItemController{
    @FXML HBox root
    @FXML ImageView imageView
    @FXML Label deviceName
    @FXML Label socketStatus
    @FXML Circle runStatus

    DeviceItemController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/device_item.fxml"))
        fxmlLoader.setController(this)
        fxmlLoader.load()
    }

    Pane getView(){
        return root
    }


    def setDeviceItem(DeviceItem deviceItem) {
        imageView.setImage(new Image(getClass().getResourceAsStream(deviceItem.isUsbConnect()?"/image/ic_settings_input_hdmi.png":"/image/ic_settings_input_antenna.png")))
        deviceName.setText("$deviceItem.brand-$deviceItem.model")
        def address=deviceItem.getDeviceProperty(Property.DEVICE_ADDRESS)
        if(address){
            socketStatus.setText(address)
        } else {
            socketStatus.setText(deviceItem.getDeviceProperty(Property.DEVICE_IMEI))
        }
        JFXDepthManager.setDepth(runStatus,2)
        runStatus.setFill(new Color(1.0,0,0,1.0))
    }
}
