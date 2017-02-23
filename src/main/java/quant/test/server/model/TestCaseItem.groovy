package quant.test.server.model

/**
 * Created by cz on 2017/2/22.
 * 测试用例条目
 * 测试用例名称/测试主apk/测试用例apk
 */
class TestCaseItem {
    String name
    String apkPath
    String testPath

    TestCaseItem(String name, String apkPath, String testPath) {
        this.name = name
        this.apkPath = apkPath
        this.testPath = testPath
    }
}
