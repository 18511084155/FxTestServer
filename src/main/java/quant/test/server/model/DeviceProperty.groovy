package quant.test.server.model

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty

/**
 * Created by cz on 2017/2/16.
 */
class DeviceProperty extends RecursiveTreeObject<DeviceProperty> {
    StringProperty key
    StringProperty value

    DeviceProperty(key, value) {
        this.key = new SimpleStringProperty(key)
        this.value = new SimpleStringProperty(value)
    }
}
