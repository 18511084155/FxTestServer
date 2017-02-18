package quant.test.server.controller
import com.jfoenix.controls.JFXTreeTableColumn
import com.jfoenix.controls.JFXTreeTableView
import com.jfoenix.controls.RecursiveTreeItem
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.event.Event
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Label
import quant.test.server.anntation.FXMLLayout
import quant.test.server.bus.RxBus
import quant.test.server.event.OnDeviceSelectedEvent
import quant.test.server.model.DeviceProperty
import quant.test.server.model.Property

import javax.annotation.PreDestroy
/**
 * Created by cz on 2017/2/16.
 */
@FXMLLayout("fxml/device_info_layout.fxml")
class DeviceInfoController implements Initializable{
    @FXML
    Label mobileModel
    @FXML
    Label mobileBrand
    @FXML
    Label mobileSerialno
    @FXML
    Label mobileImei
    @FXML
    Label mobileVersion
    @FXML
    Label mobileVersionCode
    @FXML
    Label mobileCpu
    @FXML
    Label mobileIpAddress
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

        RxBus.subscribe(OnDeviceSelectedEvent.class){
            mobileModel.setText(it.item.toString())
            mobileBrand.setText(it.item.brand)
            mobileSerialno.setText(it.item.serialNumber)
            mobileImei.setText(it.item.serialNumber)
            mobileVersion.setText(it.item.sdk)
            mobileVersionCode.setText(it.item.release)
            mobileCpu.setText(it.item.getDeviceProperty(Property.RO_PRODUCT_CPU_ABI))
            mobileIpAddress.setText(it.item.getDeviceProperty(Property.DHCP_WLAN0_IPADDRESS))

            deviceProperties.clear()
            it.item.deviceProperty.each { property->
                deviceProperties.add(new DeviceProperty(property.key,property.value))
            }
            if(Platform.fxApplicationThread){
                treeTableView.setRoot(new RecursiveTreeItem<DeviceProperty>(deviceProperties, { it.getChildren() }))
                treeTableView.setShowRoot(false)
            } else {
                Platform.runLater({
                    treeTableView.setRoot(new RecursiveTreeItem<DeviceProperty>(deviceProperties, { it.getChildren() }))
                    treeTableView.setShowRoot(false)
                })
            }
        }
    }


    @PreDestroy
    public void destroy(){
        RxBus.unSubscribeItems(this)
    }




    public void handleEnvClick(Event event) {

    }

}
