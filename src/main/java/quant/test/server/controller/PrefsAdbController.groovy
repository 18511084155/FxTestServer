package quant.test.server.controller
import com.jfoenix.controls.*
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.Event
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TreeTableView
import javafx.scene.layout.StackPane
import quant.test.server.StageManager
import quant.test.server.model.EnvironmentItem
import quant.test.server.prefs.PrefsKey
import quant.test.server.prefs.SharedPrefs
/**
 * Created by cz on 2017/2/15.
 */
class PrefsAdbController implements Initializable{
    @FXML StackPane root
    @FXML JFXTextField pathField
    @FXML JFXTreeTableView<EnvironmentItem> treeTableView
    @FXML JFXTreeTableColumn envKey
    @FXML JFXTreeTableColumn envValue
    @FXML JFXTreeTableColumn envValid
    @FXML JFXButton applyButton
    @FXML JFXSnackbar snackBar;

    @FXML
    public void handleEnvClick(Event event) {
        TreeTableView treeTableView=event.source as TreeTableView
        def item=treeTableView.getSelectionModel().getSelectedItem()
        if(item){
            pathField.setText(item.value.value.value)
        }
    }

    @Override
    void initialize(URL location, ResourceBundle resources) {
        snackBar.registerSnackbarContainer(root);

        def pathSeparator=System.properties["path.separator"]
        def fileSeparator=System.properties["file.separator"]
        def path="android${fileSeparator}sdk${fileSeparator}platform-tools"
        ObservableList<EnvironmentItem> envItems = FXCollections.observableArrayList();
        System.getenv().each {
            def valueArray = it.value.split(pathSeparator)
            if(valueArray){
                valueArray.each { value-> envItems.add(new EnvironmentItem(it.key,value,value.toLowerCase().contains(path)))}
            }
        }
        envKey.setCellValueFactory({ envKey.validateValue(it)?it.value.value.key: envKey.getComputedValue(it) })
		envValue.setCellValueFactory({ envValue.validateValue(it)? it.value.value.value: envValue.getComputedValue(it) })
		envValid.setCellValueFactory({ envValid.validateValue(it)? it.value.value.valid: envValid.getComputedValue(it) })

        int index=envItems.findIndexOf {it.valid.get()}
        if(index) {
            pathField.setText(envItems[index].value?.get())
            treeTableView.getSelectionModel().select(index);
        }

        treeTableView.setRoot(new RecursiveTreeItem<EnvironmentItem>(envItems, { it.getChildren() }))
        treeTableView.setShowRoot(false);

        //文字变化监听
        pathField.textProperty().addListener({ observable,old,newValue->
            pathField.validate() } as ChangeListener)

        //应用设置
        applyButton.setOnMouseClicked({ event->
            if(!pathField.validate()){
                snackBar.fireEvent(new JFXSnackbar.SnackbarEvent("Adb目录异常,请重新设置 ",null,2000, null))
            } else {
                def adbText=pathField.text
                //判断是否以Separator结尾,不为则补足
                adbText.endsWith(fileSeparator)?:(adbText+=fileSeparator)

                //保存sdk目录
                def file=new File(adbText)
                def sdkPath=!file.exists()?:file.parentFile.absolutePath

                if(System.properties["os.name"].startsWith("Windows")){
                    //windows  file.separator=/  path.separator=;
                    adbText+="adb.exe"
                } else if(System.properties["os.name"].startsWith("Mac")){
                    //osx file.separator=\  path.separator=:
                    adbText+="adb"
                }
                //保存路径
                SharedPrefs.save(PrefsKey.ADB,adbText)
                SharedPrefs.save(PrefsKey.SDK,sdkPath)
                def stageManager=StageManager.instance
                stageManager.getStage(this)?.hide()
                stageManager.newStage(getClass().getClassLoader().getResource("fxml/main_layout.fxml"),800,720)?.show()
            }
        } as EventHandler)
    }

}
