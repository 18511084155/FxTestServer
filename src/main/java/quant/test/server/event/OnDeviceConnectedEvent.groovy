package quant.test.server.event

import quant.test.server.model.DeviceItem

/**
 * Created by cz on 2017/3/6.
 */
class OnDeviceConnectedEvent {
    final DeviceItem deviceItem

    OnDeviceConnectedEvent(DeviceItem deviceItem) {
        this.deviceItem = deviceItem
    }
}
