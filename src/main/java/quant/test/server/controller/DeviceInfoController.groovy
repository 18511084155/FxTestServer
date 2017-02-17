package quant.test.server.controller
import com.jfoenix.controls.JFXTreeTableColumn
import com.jfoenix.controls.JFXTreeTableView
import com.jfoenix.controls.RecursiveTreeItem
import javafx.collections.FXCollections
import javafx.event.Event
import javafx.fxml.FXML
import javafx.fxml.Initializable
import quant.test.server.anntation.FXMLLayout
import quant.test.server.bus.RxBus
import quant.test.server.event.OnDeviceConnectedEvent
import quant.test.server.model.DeviceProperty

import javax.annotation.PreDestroy
/**
 * Created by cz on 2017/2/16.
 */
@FXMLLayout("fxml/device_info_layout.fxml")
class DeviceInfoController implements Initializable{
    @FXML
    JFXTreeTableView treeTableView
    @FXML
    JFXTreeTableColumn deviceKey
    @FXML
    JFXTreeTableColumn deviceValue

    def deviceProperties

    @Override
    void initialize(URL location, ResourceBundle resources) {
        deviceProperties = FXCollections.observableArrayList()
        deviceKey.setCellValueFactory({ deviceKey.validateValue(it)?it.value.value.key: deviceKey.getComputedValue(it) })
        deviceValue.setCellValueFactory({ deviceValue.validateValue(it)? it.value.value.value: deviceValue.getComputedValue(it) })

        RxBus.subscribe(OnDeviceConnectedEvent.class){
            deviceProperties.clear()
            it.item.deviceProperty.each { property->
                deviceProperties.add(new DeviceProperty(property.key,property.value))
            }
            treeTableView.setRoot(new RecursiveTreeItem<DeviceProperty>(deviceProperties, { it.getChildren() }))
            treeTableView.setShowRoot(false)
        }
    }


    @PreDestroy
    public void destroy(){
        RxBus.unSubscribeItems(this)
    }




    public void handleEnvClick(Event event) {

    }

}
