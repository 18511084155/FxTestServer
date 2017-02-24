package quant.test.server.controller
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXSpinner
import com.jfoenix.controls.JFXTextField
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TextArea
import javafx.stage.FileChooser
import quant.test.server.StageManager
import quant.test.server.command.Command
import quant.test.server.widget.drag.DragTextField

import java.util.concurrent.Executors

/**
 * Created by cz on 2017/2/23.
 */
class AddTestCaseController implements Initializable{

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
    def file1,file2

    @Override
    void initialize(URL location, ResourceBundle resources) {

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
        applyButton.setOnMouseClicked({stage.hide()})
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
     * 处理文件
     * @param file
     */
    def processApkFile(file,fileChoose,apkSpinner) {
        if(file){
            //TODO 脚本位置
            fileChoose.setVisible(false)
            apkSpinner.setVisible(true)
            messageArea.appendText("开始解析文件:$file.absolutePath\n")
            Executors.newSingleThreadScheduledExecutor().execute({
                def result=Command.shell("/Users/cz/Desktop/master/FxTestServer/src/main/resources/script/apk_file.sh",file.absolutePath)
                if(0<=result.exit){
                    final def params=result.out2Map()
                    Platform.runLater({
                        fileChoose.setVisible(true)
                        apkSpinner.setVisible(false)
                        messageArea.appendText("应用名:${params["label"]}\n")
                        messageArea.appendText("应用包名:${params["package"]}\n")
                        messageArea.appendText("应用版号:${params["versionName"]}\n")
                        messageArea.appendText("最低版本:${params["sdkVersion"]}\n")
                        messageArea.appendText("最高版本:${params["targetSdkVersion"]}\n")
                        messageArea.appendText("应用签名:${params["md5"]}\n")
                        messageArea.appendText("================文件信息分析完成================\n")
                    })
                }
            })
        }
    }
}
