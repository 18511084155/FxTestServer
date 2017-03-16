package quant.test.server.event

import quant.test.server.model.DeviceItem

/**
 * Created by cz on 2017/3/16.
 */
class OnDeviceStartEvent {
    final DeviceItem deviceItem

    OnDeviceStartEvent(DeviceItem deviceItem) {
        this.deviceItem = deviceItem
    }
}
