package quant.test.server.model

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty

/**
 * Created by cz on 2017/2/15.
 */
class EnvironmentItem extends RecursiveTreeObject<EnvironmentItem> {
    StringProperty key
    StringProperty value
    SimpleBooleanProperty valid

    EnvironmentItem(key, value,valid) {
        this.key = new SimpleStringProperty(key)
        this.value = new SimpleStringProperty(value)
        this.valid=new SimpleBooleanProperty(valid)
    }
}
