package quant.test.server.event

import quant.test.server.model.DeviceItem

/**
 * Created by cz on 2017/3/6.
 */
class OnActionStartEvent {
    final DeviceItem deviceItem

    OnActionStartEvent(DeviceItem deviceItem) {
        this.deviceItem = deviceItem
    }
}
