package quant.test.server.event

import quant.test.server.model.DeviceItem

/**
 * Created by cz on 2017/2/16.
 */
class OnDeviceSelectedEvent {
    DeviceItem item

    OnDeviceSelectedEvent(DeviceItem item) {
        this.item = item
    }
}
