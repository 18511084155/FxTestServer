package quant.test.server.model

/**
 * Created by cz on 2017/2/22.
 * 测试用例条目
 * 测试用例名称/测试主apk/测试用例apk
 */
class TestCaseItem {
    int id//用例id
    long ct//创建时间
    int uid//操作用例用户uid
    String name//用例名称
    String apk1//主包
    String apk2//测试用例包
    String appName//应用名称
    String appVersion//应用版本
    String apk1Md5//应用签名md5
    String apk2Md5//测试用例签名md5


    TestCaseItem(String name, String apkPath, String testPath) {
        this.name = name
        this.apk1 = apkPath
        this.apk2 = testPath
    }
}
