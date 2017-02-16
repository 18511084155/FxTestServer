package quant.test.server.widget

import javafx.scene.control.ListCell
import quant.test.server.controller.DeviceItemController
import quant.test.server.model.DeviceItem

/**
 * Created by czz on 2017/2/15.
 */
class DeviceListCell extends ListCell<DeviceItem> {
    private final DeviceItemController controller = new DeviceItemController();
    private final def view = controller.getView();
    @Override
    protected void updateItem(DeviceItem item, boolean empty) {
        super.updateItem(item, empty)
        if (empty) {
            setGraphic(null)
        } else {
            controller.setDeviceItem(item)
            setGraphic(view)
        }
    }

}
