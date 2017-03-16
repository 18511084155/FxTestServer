package quant.test.server.widget

import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ChangeListener
import javafx.scene.control.ContextMenu
import javafx.scene.control.ListCell
import javafx.scene.control.MenuItem
import quant.test.server.controller.DeviceItemController
import quant.test.server.model.DeviceItem
/**
 * Created by czz on 2017/2/15.
 */
class DeviceListCell extends ListCell<DeviceItem> {
    private final DeviceItemController controller = new DeviceItemController();
    private final def view = controller.getView()
    def startAction,stopAction

    DeviceListCell() {
    }

    /**
     * 初始化上下文菜单
     * @return
     */
    def initContextMenu(){
        ContextMenu contextMenu = new ContextMenu()
        MenuItem editItem = new MenuItem()
        editItem.textProperty().bind(new SimpleStringProperty("启动任务"))
        editItem.setOnAction({ startAction?.call(getItem()) })

        MenuItem stopItem = new MenuItem();
        stopItem.textProperty().bind(new SimpleStringProperty("终止任务"))
        stopItem.setOnAction({ stopAction?.call(getItem()) })

        contextMenu.getItems().addAll(editItem,stopItem)
//        textProperty().bind(new SimpleStringProperty(deviceInfo))

        emptyProperty().addListener({obs, wasEmpty, isNowEmpty->
            if (isNowEmpty) {
                setContextMenu(null)
            } else {
                setContextMenu(contextMenu)
            }
        } as ChangeListener)
    }

    @Override
    protected void updateItem(DeviceItem item, boolean empty) {
        super.updateItem(item, empty)
        if (empty) {
            setGraphic(null)
        } else {
            controller.bindItem(item)
            setGraphic(view)
        }
        initContextMenu()
    }

    def setStartAction(closure){
        this.startAction=closure
    }


    def setStopAction(closure){
        this.stopAction=closure
    }

}
