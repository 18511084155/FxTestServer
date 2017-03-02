package quant.test.server.model

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleStringProperty

/**
 * Created by cz on 2017/2/27.
 */
class TestCaseProperty extends RecursiveTreeObject<TestCaseProperty> {
    SimpleIntegerProperty id//用例id
    SimpleIntegerProperty uid//操作用例用户uid
    SimpleIntegerProperty sdkVersion
    SimpleIntegerProperty targetSdkVersion
    SimpleStringProperty name//用例名称
    SimpleStringProperty apk1//主包
    SimpleStringProperty apk2//测试用例包
    SimpleStringProperty appName//应用名称
    SimpleStringProperty apkPackage//应用包名
    SimpleStringProperty testPackage//测试应用包名
    SimpleStringProperty appVersion//应用版本
    SimpleStringProperty apk1Md5//应用签名md5
    SimpleStringProperty apk2Md5//测试用例签名md5
    SimpleLongProperty ct

    TestCaseProperty(TestCaseItem item){
        id=new SimpleIntegerProperty(item.id)
        uid=new SimpleIntegerProperty(item.uid)
        sdkVersion=new SimpleIntegerProperty(item.sdkVersion)
        targetSdkVersion=new SimpleIntegerProperty(item.targetSdkVersion)
        name=new SimpleStringProperty(item.name)
        apk1=new SimpleStringProperty(item.apk1)
        apk2=new SimpleStringProperty(item.apk2)
        appName=new SimpleStringProperty(item.appName)
        apkPackage=new SimpleStringProperty(item.apkPackage)
        testPackage=new SimpleStringProperty(item.testPackage)
        appVersion=new SimpleStringProperty(item.appVersion)
        apk1Md5=new SimpleStringProperty(item.apk1Md5)
        apk2Md5=new SimpleStringProperty(item.apk2Md5)
        ct=new SimpleLongProperty(item.ct)
    }

}
