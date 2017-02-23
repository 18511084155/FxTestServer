package quant.test.server.model
/**
 * Created by cz on 12/8/16.
 ro.product.model=MX5
 ro.product.brand=Meizu
 ro.build.version.sdk=22
 ro.build.version.release=5.1
 gsm.serial=85GBBMK223XJ
 */
class DeviceItem {
    def model
    def brand
    def sdk
    def release
    def state
    def gsmSerial
    def serialNumber
    def properties

    static DeviceItem form(device){
        new DeviceItem(device.serialNumber,
                device.properties[Property.RO_PRODUCT_BRAND],
                device.properties[Property.RO_PRODUCT_MODEL],
                device.properties[Property.RO_BUILD_VERSION_RELEASE],
                device.properties[Property.RO_BUILD_VERSION_SDK],
                device.properties[Property.RO_SERIALNO],
                device.state,device.properties)
    }

    DeviceItem(serialNumber,model, brand, sdk, release, gsmSerial,state,properties) {
        this.serialNumber=serialNumber
        this.model = model
        this.brand = brand
        this.sdk = sdk
        this.release = release
        this.gsmSerial = gsmSerial
        this.state=state
        this.properties=[:]
        !properties?:(this.properties+=properties)
    }

    @Override
    int hashCode() {
        Objects.hash(model,brand,sdk,release,gsmSerial)
    }

    def isUsbConnect(){
        serialNumber.equals(gsmSerial)
    }

    def getDeviceProperty(){
        properties
    }

    def getDeviceProperty(key){
        properties[key]
    }

    @Override
    boolean equals(Object obj) {
        if (is(obj)) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DeviceItem that = (DeviceItem) obj;
        return Objects.equals(model, that.model) &&
                Objects.equals(brand, that.brand) &&
                Objects.equals(sdk, that.sdk) &&
                Objects.equals(release, that.release) &&
                Objects.equals(gsmSerial, that.gsmSerial);
    }

    @Override
    String toString() {
        "$model $brand Android:$sdk ${isUsbConnect()?'Usb':'无线'}_${state?:"unknown"}"
    }
}
