package quant.test.server.controller

import com.google.common.io.Files
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXSnackbar
import com.jfoenix.controls.JFXSpinner
import com.jfoenix.controls.JFXTextField
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TextArea
import javafx.scene.layout.StackPane
import javafx.stage.FileChooser
import quant.test.server.StageManager
import quant.test.server.bus.RxBus
import quant.test.server.command.Command
import quant.test.server.database.DbHelper
import quant.test.server.event.OnTestCaseAddedEvent
import quant.test.server.log.Log
import quant.test.server.model.TestCaseItem
import quant.test.server.prefs.FilePrefs
import quant.test.server.scheduler.MainThreadSchedulers
import quant.test.server.util.FileUtils
import quant.test.server.widget.drag.DragTextField
import rx.Observable
import rx.schedulers.Schedulers
/**
 * Created by cz on 2017/2/23.
 */
class AddTestCaseController implements Initializable{
    final def TAG="AddTestCaseController"
    @FXML StackPane root
    @FXML JFXTextField testCaseName
    @FXML DragTextField testCaseApk1
    @FXML JFXButton fileChoose1
    @FXML JFXSpinner apkSpinner1

    @FXML DragTextField testCaseApk2
    @FXML JFXButton fileChoose2
    @FXML JFXSpinner apkSpinner2

    @FXML TextArea messageArea
    @FXML JFXButton cancelButton
    @FXML JFXButton applyButton
    @FXML JFXSnackbar snackBar
    def testCaseItem=new TestCaseItem()
    def file1,file2

    @Override
    void initialize(URL location, ResourceBundle resources) {
        snackBar.registerSnackbarContainer(root)
        final def stage=StageManager.instance.getStage(this)

        //设置命名名称个数
        testCaseName.focusedProperty().addListener({observable,oldValue,newValue->
            newValue?:testCaseName.validate() } as ChangeListener)

        //设置拖动事件
        setDraggedChanged(testCaseApk1,{processApkFile(file1=it,fileChoose1,apkSpinner1)})
        setDraggedChanged(testCaseApk2,{processApkFile(file2=it,fileChoose2,apkSpinner2)})

        //设置点击事件
        setButtonMouseClicked(fileChoose1,stage,{processApkFile(file1=it,fileChoose1,apkSpinner1)})
        setButtonMouseClicked(fileChoose2,stage,{processApkFile(file2=it,fileChoose2,apkSpinner2)})

        //关闭窗口
        cancelButton.setOnMouseClicked({StageManager.instance.getStage(this)?.hide()})
        //添加记录
        applyButton.setOnMouseClicked({applyTestCase()})
    }

    def applyTestCase() {
        File targetFolder
        if(!testCaseName.text){
            snackBar.fireEvent(new JFXSnackbar.SnackbarEvent("请输入测试用例名称!!",null,2000, null))
        } else if((targetFolder=new File(FilePrefs.TEST_CASE_FOLDER, testCaseName.text)).exists()){
            snackBar.fireEvent(new JFXSnackbar.SnackbarEvent("该测试用例己存在,请重新设置用例名称!",null,2000, null))
        } else if(!testCaseItem.apk1){
            snackBar.fireEvent(new JFXSnackbar.SnackbarEvent("请选择测试包!",null,2000, null))
        } else if(!testCaseItem.apk2){
            snackBar.fireEvent(new JFXSnackbar.SnackbarEvent("请选择测试用例包!",null,2000, null))
        } else if(!validatorPackage(testCaseItem.apkPackage,testCaseItem.testPackage)){
            snackBar.fireEvent(new JFXSnackbar.SnackbarEvent("测试包名与用例包名不一致,请检测!",null,2000, null))
        } else if(testCaseItem.apk1Md5!=testCaseItem.apk2Md5){
            snackBar.fireEvent(new JFXSnackbar.SnackbarEvent("测试包与用例包签名不一致,请检测!",null,2000, null))
        } else {
            Observable.create({
                //拷贝文件到应用目录
                File targetFile1,targetFile2
                (targetFile1,targetFile2)=copySrc2Target(targetFolder)
                //拷贝成功后的目录重新赋给对象,并保存数据库
                testCaseItem.apk1=targetFile1.absolutePath
                testCaseItem.apk2=targetFile2.absolutePath
                //插入条目
                testCaseItem.name=testCaseName.text
                boolean result=DbHelper.helper.insertTestCase(testCaseItem)
                it.onNext(result)
                it.onCompleted()
            }).subscribeOn(Schedulers.io()).
                observeOn(MainThreadSchedulers.mainThread()).
                subscribe({
                    //通知信息添加
                    Log.e(TAG,"用户:$testCaseItem.uid 添加测试用例:$testCaseItem.name 成功!")
                    RxBus.post(new OnTestCaseAddedEvent(testCaseItem))
                    //关闭窗口,这里从上面传入,会导致null,所以重新取.
                    StageManager.instance.getStage(this)?.hide()
                },{it.printStackTrace()})
        }
    }

    /**
     * 拷贝文件到目录
     */
    def copySrc2Target(File targetFolder) {
        targetFolder.exists()?:targetFolder.mkdir()
        def pathSeparator = System.properties["file.separator"]
        def apkPath1 = testCaseItem.apk1
        def apkPath2 = testCaseItem.apk2
        File srcFile1 = new File(apkPath1)
        File srcFile2 = new File(apkPath2)
        File targetFile1 = new File(targetFolder, apkPath1.substring(apkPath1.lastIndexOf(pathSeparator) + 1))
        File targetFile2 = new File(targetFolder, apkPath2.substring(apkPath2.lastIndexOf(pathSeparator) + 1))
        Files.copy(srcFile1, targetFile1)
        Files.copy(srcFile2, targetFile2)
        [targetFile1,targetFile2]
    }

    def setDraggedChanged(textField,closure){
        textField.setDragListener(){ File file->
            if(file&&file.exists()&&file.name.endsWith(".apk")){
                closure(file)
            } else {
                textField.setText("")
            }
        }
    }


    /**
     * 设置文件选择器的点击
     * @param button
     * @param stage
     * @param closure
     * @return
     */
    def setButtonMouseClicked(button,stage,closure){
        button.setOnMouseClicked({
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("请选择一个Android安装文件");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("APK", "*.apk"));
            def file=fileChooser.showOpenDialog(stage)
            if(file&&file.exists()){
                closure(file)
            }
        })
    }

    /**
     * 检测应用包名
     * @param package1
     * @param package2
     * @return
     */
    boolean validatorPackage(String package1, String package2) {
        def result=false
        if(package1!=package2){
            !package1.endsWith(".test")?:(result=package1.startsWith(package2))
            !package2.endsWith(".test")?:(result=package2.startsWith(package1))
        }
        result
    }

    /**
     * 处理文件
     * @param file
     */
    def processApkFile(file,fileChoose,apkSpinner) {
        if(file){
            fileChoose.setVisible(false)
            apkSpinner.setVisible(true)
            messageArea.appendText("开始分析文件:$file.absolutePath\n")
            Observable.create({
                FileUtils.copyResourceFileIfNotExists(FilePrefs.SCRIPT_SCAN_APK, "script/apk_file.sh");
                def result=Command.shell(FilePrefs.SCRIPT_SCAN_APK.absolutePath,file.absolutePath)
                it.onNext(result)
                it.onCompleted()
            }).subscribeOn(Schedulers.io()).
                    observeOn(MainThreadSchedulers.mainThread()).
                    subscribe({ processApkResult(it,file,fileChoose,apkSpinner) },{it.printStackTrace()})
        }
    }

    /**
     * 处理解析包结果
     * @param result
     * @param fileChoose
     * @param apkSpinner
     * @return
     */
    def processApkResult(Command.Result result,file,fileChoose,apkSpinner) {
        if(0<=result.exit){
            final def params=result.out2LazyMap()
            Platform.runLater({
                fileChoose.setVisible(true)
                apkSpinner.setVisible(false)
                messageArea.appendText("应用包名:${params.package}\n")
                if(params.test){
                    //测试用例
                    testCaseItem.apk2=file.absolutePath
                    testCaseItem.apk2Md5=params.md5
                    testCaseItem.testPackage=params.package
                } else {
                    testCaseItem.apk1=file.absolutePath
                    testCaseItem.apk1Md5=params.md5
                    testCaseItem.appName=params.label
                    testCaseItem.appVersion=params.versionName
                    testCaseItem.sdkVersion=params.sdkVersion
                    testCaseItem.apkPackage=params.package
                    testCaseItem.targetSdkVersion=params.targetSdkVersion
                    messageArea.appendText("应用名:${params.label}\n")
                    messageArea.appendText("应用版号:${params.versionName}\n")
                }
                messageArea.appendText("最低版本:${params.sdkVersion}\n")
                messageArea.appendText("最高版本:${params.targetSdkVersion}\n")
                messageArea.appendText("应用签名:${params.md5}\n")
                messageArea.appendText("================文件信息分析完成================\n")

                //当两个目录全部填写完毕后
                if(testCaseItem.apk1&&testCaseItem.apk2){
                    if(!validatorPackage(testCaseItem.apkPackage,testCaseItem.testPackage)){
                        messageArea.appendText("测试包名与用例包名不一致,请检测!\n")
                        snackBar.fireEvent(new JFXSnackbar.SnackbarEvent("测试包名与用例包名不一致,请检测!",null,2000, null))
                    } else if(testCaseItem.apk1Md5!=testCaseItem.apk2Md5){
                        messageArea.appendText("测试包与用例包签名不一致,请检测!\n")
                        snackBar.fireEvent(new JFXSnackbar.SnackbarEvent("测试包与用例包签名不一致,请检测!",null,2000, null))
                    } else {
                        messageArea.appendText("应用程序与测试用例匹配完成,可以添加\n")
                        snackBar.fireEvent(new JFXSnackbar.SnackbarEvent("包检测通过,可以添加!",null,2000, null))
                    }
                }
            })
        }
    }
}
