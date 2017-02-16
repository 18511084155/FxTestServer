package quant.test.server.event

import quant.test.server.model.DeviceItem

/**
 * Created by cz on 2017/2/16.
 */
class OnDeviceConnectedEvent {
    DeviceItem item

    OnDeviceConnectedEvent(DeviceItem item) {
        this.item = item
    }
}
