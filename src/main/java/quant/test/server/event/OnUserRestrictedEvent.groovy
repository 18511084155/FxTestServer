package quant.test.server.event

/**
 * Created by cz on 2017/3/15.
 */
class OnUserRestrictedEvent {
    final def address

    OnUserRestrictedEvent(address) {
        this.address = address
    }
}
