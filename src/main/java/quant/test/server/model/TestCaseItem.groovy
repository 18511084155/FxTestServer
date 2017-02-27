package quant.test.server.model
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleStringProperty
/**
 * Created by cz on 2017/2/22.
 * 测试用例条目
 * 测试用例名称/测试主apk/测试用例apk
 */
class TestCaseItem {
    int id//用例id
    int uid//操作用例用户uid
    int sdkVersion
    int targetSdkVersion
    String name//用例名称
    String apk1//主包
    String apk2//测试用例包
    String appName//应用名称
    String apkPackage//应用包名
    String testPackage//测试应用包名
    String appVersion//应用版本
    String apk1Md5//应用签名md5
    String apk2Md5//测试用例签名md5
    long ct//创建时间

    TestCaseItem() {
    }

    TestCaseItem(String name, String apkPath, String testPath) {
        this.name = name
        this.apk1 = apkPath
        this.apk2 = testPath
    }

    TestCaseProperty createProperty(){
        def property=new TestCaseProperty()
        property.id=new SimpleIntegerProperty(id)
        property.uid=new SimpleIntegerProperty(uid)
        property.sdkVersion=new SimpleIntegerProperty(sdkVersion)
        property.targetSdkVersion=new SimpleIntegerProperty(targetSdkVersion)
        property.name=new SimpleStringProperty(name)
        property.apk1=new SimpleStringProperty(apk1)
        property.apk2=new SimpleStringProperty(apk2)
        property.appName=new SimpleStringProperty(appName)
        property.apkPackage=new SimpleStringProperty(apkPackage)
        property.testPackage=new SimpleStringProperty(testPackage)
        property.appVersion=new SimpleStringProperty(appVersion)
        property.apk1Md5=new SimpleStringProperty(apk1Md5)
        property.apk2Md5=new SimpleStringProperty(apk2Md5)
        property.ct=new SimpleLongProperty(ct)
        property
    }
}
