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
import quant.test.server.bus.RxBus
import quant.test.server.database.DbHelper
import quant.test.server.event.OnTestCaseAddedEvent
import quant.test.server.event.OnTestPlanAddedEvent
import quant.test.server.model.TestCaseItem
import quant.test.server.model.TestPlanItem
import rx.Observable
import rx.Subscriber
import rx.Subscription
import rx.schedulers.Schedulers
/**
 * Created by Administrator on 2017/2/19.
 */
@FXMLLayout("fxml/task_layout.fxml")
class TestPlanController implements Initializable{
    @FXML ScrollPane scrollPane
    @FXML JFXMasonryPane masonryPane
    @FXML JFXButton addTask
    def colorItems=["#FFFFFF","#8F3F7E","#B5305F","#CE584A","#DB8D5C","#DA854E","#E9AB44","#FEE435","#99C286","#01A05E","#4A8895","#16669B","#2F65A5","#4E6A9C"]
    def planItems=[]
    @Override
    void initialize(URL location, ResourceBundle resources) {
        initTestPlanItems()
        initTestCaseItems()

        JFXDepthManager.setDepth(addTask, 1)
        Platform.runLater({scrollPane.requestLayout()})

        //如果没有一个测试用例,则不允许添加任务计划
        RxBus.subscribe(OnTestCaseAddedEvent.class,{ initAddButton() })
        //添加一个测试计划
        RxBus.subscribe(OnTestPlanAddedEvent.class,{
            planItems<<it.item
            addTaskPlan(it.item)
        })
    }

    /**
     * 初始化测试计划条目
     * @return
     */
    private Subscription initTestPlanItems() {
        Observable.create({ Subscriber subscriber ->
            def items = DbHelper.helper.queryTestPlan()
            !items ?: subscriber.onNext(items)
            subscriber.onCompleted()
        } as Observable.OnSubscribe<List<TestCaseItem>>).
                subscribeOn(Schedulers.io()).subscribe({ items ->
                        planItems+=items
                        Platform.runLater({ items.each { addTaskPlan(it) }
                    })
        }, { it.printStackTrace() })
    }

    /**
     * 初始化测试用例条目
     */
    def initTestCaseItems() {
        Observable.create({ Subscriber subscriber ->
            def items = DbHelper.helper.queryTestCase()
            !items ?: subscriber.onNext(items)
            subscriber.onCompleted()
        } as Observable.OnSubscribe<List<TestCaseItem>>).
                subscribeOn(Schedulers.io()).subscribe({ items -> initAddButton() }, { it.printStackTrace() })
    }

    private initAddButton() {
        if(Platform.fxApplicationThread){
            addTask.setDisable(false)
            addTask.setOnMouseClicked({
                StageManager.instance.newStage(getClass().getClassLoader().getResource("fxml/add_task_layout.fxml"), planItems, 640, 720)?.show()
            })
        } else {
            Platform.runLater({
                //设置控件可点击
                addTask.setDisable(false)
                addTask.setOnMouseClicked({
                    StageManager.instance.newStage(getClass().getClassLoader().getResource("fxml/add_task_layout.fxml"), planItems, 640, 720)?.show()
                })
            })
        }
    }

    def addTaskPlan(TestPlanItem item){
        def child=FXMLLoader.load(getClass().getClassLoader().getResource("fxml/task_item.fxml"))
        child.setStyle("-fx-background-color: ${colorItems[(int) ((Math.random()*12)%12)]}")
        JFXDepthManager.setDepth(child, 1)
        Label planLabel=child.lookup("#planLabel")
        planLabel.setText(item.name)
        Label caseLabel=child.lookup("#caseLabel")
        caseLabel.setText(item.testCase)

        Label startTimeLabel=child.lookup("#startTimeLabel")
        startTimeLabel.setText(item.startDate)
        Label endTimeLabel=child.lookup("#endTimeLabel")
        endTimeLabel.setText(item.endDate)
        Label cycleLabel=child.lookup("#cycleLabel")
        cycleLabel.setText("循环执行:${item.cycle?"YES":"NO"}")
        masonryPane.children.add(child)
    }

}
