package quant.test.server.controller
import com.jfoenix.controls.JFXCheckBox
import com.jfoenix.controls.JFXTextField
import com.jfoenix.controls.JFXToggleNode
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ComboBox
import org.fxmisc.richtext.StyleClassedTextArea
import quant.test.server.anntation.FXMLLayout
import quant.test.server.log.Log
import quant.test.server.log.LogItem

import javax.annotation.PreDestroy
/**
 * Created by cz on 2017/2/17.
 */
@FXMLLayout("fxml/message_layout.fxml")
class MessageController implements Initializable,Observer{
    final def levelColor=["black","green","red"]
    @FXML StyleClassedTextArea messageArea
    @FXML ComboBox<String> comboBox
    @FXML JFXTextField searchField
    @FXML JFXCheckBox regexCheckBox
    @FXML JFXToggleNode staticNode
    @Override
    void initialize(URL location, ResourceBundle resources) {
        Log.registerObservable(this)
        initComboBox()
        initFilterLog()
        staticNode.selectedProperty().addListener({ observable, oldValue, newValue ->

        } as ChangeListener<Boolean>)
    }

    /**
     * 初始化过滤日志
     * @return
     */
    private initFilterLog() {
        searchField.textProperty().addListener({ observable, oldValue, newValue ->
            //如果为空,则按等级按钮
            //如果不为空,则按是否为正则过滤
            def logItems
            int selectedIndex = comboBox.selectionModel.selectedIndex
            if (!newValue) {
                logItems = Log.filterLevel(selectedIndex)
            } else {
                if (regexCheckBox.isSelected()) {
                    logItems = Log.matcherLog(selectedIndex, newValue)
                } else {
                    logItems = Log.filterLog(selectedIndex, newValue)
                }
            }
            messageArea.clear()
            println "filter:"+logItems?.size()
            logItems?.each { appendLogItem(it) }
        } as ChangeListener<String>)
    }
    /**
     * 初始化日志等级的过滤选择列表
     */
    private void initComboBox() {
        comboBox.getItems().addAll(["INFO", "WARNING", "ERROR"])
        comboBox.getSelectionModel().selectFirst()
        comboBox.valueProperty().addListener({ observable, oldValue, newValue ->
            messageArea.clear()
            int selectedIndex = comboBox.selectionModel.selectedIndex
            def logItems = Log.filterLevel(selectedIndex)
            logItems.each { appendLogItem(it) }
        } as ChangeListener<String>)
    }

    @PreDestroy
    void onDestroy(){
        Log.unregisterObservable(this)
    }

    @Override
    void update(Observable o, Object arg) {
        def logItem=arg as LogItem
        int selectedIndex = comboBox.selectionModel.selectedIndex
        if(logItem&&Log.matcherLog(logItem,selectedIndex,searchField.text,regexCheckBox.isSelected())){
            if(Platform.fxApplicationThread){
                appendLogItem(logItem)
            } else {
                Platform.runLater({appendLogItem(logItem)})
            }
        }
    }

    /**
     * 添加一个日志
     * @param logItem
     */
    void appendLogItem(LogItem logItem) {
        def logText = logItem.toString()+"\n"
        final int textLength = logText.length()
        final int length = messageArea.text.length()
        if(!staticNode.isSelected()){
            messageArea.appendText(logText)
        } else {
            int caretPosition = messageArea.caretPositionProperty().value
            messageArea.appendText(logText)
            messageArea.positionCaret(caretPosition);
        }
        messageArea.setStyleClass(length, length + textLength, levelColor[logItem.level])
    }
}
