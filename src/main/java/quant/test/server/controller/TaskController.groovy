package quant.test.server.controller

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXMasonryPane
import com.jfoenix.effects.JFXDepthManager
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import quant.test.server.StageManager
import quant.test.server.anntation.FXMLLayout

/**
 * Created by Administrator on 2017/2/19.
 */
@FXMLLayout("fxml/task_layout.fxml")
class TaskController implements Initializable{
    @FXML ScrollPane scrollPane
    @FXML JFXMasonryPane masonryPane
    @FXML JFXButton addTask
    def colorItems=["#FFFFFF","#8F3F7E","#B5305F","#CE584A","#DB8D5C","#DA854E","#E9AB44","#FEE435","#99C286","#01A05E","#4A8895","#16669B","#2F65A5","#4E6A9C"]
    @Override
    void initialize(URL location, ResourceBundle resources) {
        0.step(8,1){
            def child=FXMLLoader.load(getClass().getClassLoader().getResource("fxml/task_item.fxml"))
            child.setStyle("-fx-background-color: ${colorItems[(int) ((Math.random()*12)%12)]}")
//            int height = (int)(Math.random()*80 + 60)
//            child.setMinHeight(height)
//            child.setMaxHeight(height)
//            child.setPrefHeight(height)
            JFXDepthManager.setDepth(child, 1)
            Label timeLabel=child.lookup("#timeLabel")
            timeLabel.setText("timeLabel")
            Label caseLabel=child.lookup("#caseLabel")
            caseLabel.setText("测试用例1")
            Label deviceLabel=child.lookup("#deviceLabel")
            deviceLabel.setText("deviceItem")
            masonryPane.children.add(child)
        }
        JFXDepthManager.setDepth(addTask, 1)
        addTask.setOnMouseClicked({StageManager.instance.newStage(getClass().getClassLoader().getResource("fxml/add_task_layout.fxml"),480,420)?.show()})
        Platform.runLater({scrollPane.requestLayout()})
    }

}
