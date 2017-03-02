package quant.test.server.controller
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXTreeTableColumn
import com.jfoenix.controls.JFXTreeTableView
import com.jfoenix.controls.RecursiveTreeItem
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TreeItem
import quant.test.server.StageManager
import quant.test.server.anntation.FXMLLayout
import quant.test.server.bus.RxBus
import quant.test.server.database.DbHelper
import quant.test.server.event.OnTestCaseAddedEvent
import quant.test.server.model.TestCaseItem
import quant.test.server.model.TestCaseProperty
import rx.Observable
import rx.Subscriber
import rx.schedulers.Schedulers
/**
 * Created by Administrator on 2017/2/19.
 */
@FXMLLayout("fxml/testcase_layout.fxml")
class TestCaseController implements Initializable{
    @FXML JFXTreeTableView treeTableView
    @FXML JFXTreeTableColumn testCaseName
    @FXML JFXTreeTableColumn testApp
    @FXML JFXTreeTableColumn testVersion
    @FXML JFXTreeTableColumn testFile
    @FXML JFXTreeTableColumn testCaseFile
    @FXML JFXButton addTaskCase
    @Override
    void initialize(URL location, ResourceBundle resources) {
        addTaskCase.setOnMouseClicked({StageManager.instance.newStage(getClass().getClassLoader().getResource("fxml/add_task_case_layout.fxml"),720,640)?.show()})
        Observable.create({Subscriber subscriber->
            def items=DbHelper.helper.queryTestCase()
            !items?:subscriber.onNext(items)
            subscriber.onCompleted()
        } as Observable.OnSubscribe<List<TestCaseItem>>).
                subscribeOn(Schedulers.io()).subscribe({ items->
                Platform.runLater({ createTreeTable(items) })
        },{it.printStackTrace()})

        //订阅动态添加对象
        RxBus.subscribe(OnTestCaseAddedEvent.class,{
            def root=treeTableView.root
            if(!root){
                //初始化操作
                createTreeTable([it.testCaseItem])
            } else {
                //添加到顶部
                root.children.add(0,new TreeItem(new TestCaseProperty(it.testCaseItem)))
            }
        })
    }

    /**
     * 创建测试用例表
     * @param testCaseItems
     */
    def createTreeTable(List<TestCaseItem> items) {
        testCaseName.setCellValueFactory({ testCaseName.validateValue(it)?it.value.value.name: testCaseName.getComputedValue(it) })
        testApp.setCellValueFactory({ testApp.validateValue(it)? it.value.value.appName: testApp.getComputedValue(it) })
        testVersion.setCellValueFactory({ testVersion.validateValue(it)? it.value.value.appVersion: testVersion.getComputedValue(it) })
        testFile.setCellValueFactory({ testFile.validateValue(it)? it.value.value.apk1: testFile.getComputedValue(it) })
        testCaseFile.setCellValueFactory({ testCaseFile.validateValue(it)? it.value.value.apk2: testCaseFile.getComputedValue(it) })

        ObservableList<TestCaseProperty> testCaseItems = FXCollections.observableArrayList();
        items.each {testCaseItems.add(new TestCaseProperty(it))}
        treeTableView.setRoot(new RecursiveTreeItem<TestCaseProperty>(testCaseItems, { it.getChildren() }))
        treeTableView.setShowRoot(false);

    }
}
