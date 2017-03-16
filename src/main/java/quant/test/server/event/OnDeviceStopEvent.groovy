package quant.test.server.event

import quant.test.server.model.DeviceItem

/**
 * Created by cz on 2017/3/16.
 */
class OnDeviceStopEvent {
    final DeviceItem deviceItem

    OnDeviceStopEvent(DeviceItem deviceItem) {
        this.deviceItem = deviceItem
    }
}
