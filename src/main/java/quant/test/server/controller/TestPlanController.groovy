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
import javafx.util.Pair
import quant.test.server.StageManager
import quant.test.server.anntation.FXMLLayout
import quant.test.server.bus.RxBus
import quant.test.server.database.DbHelper
import quant.test.server.event.OnTestCaseAddedEvent
import quant.test.server.event.OnTestPlanAddedEvent
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
        initTestItems()

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
    private Subscription initTestItems() {
        Observable.create({ Subscriber subscriber ->
            def testPlanItems = DbHelper.helper.queryTestPlan()
            def testCaseItems = DbHelper.helper.queryTestCase()
            subscriber.onNext(new Pair(testPlanItems,testCaseItems))
            subscriber.onCompleted()
        } as Observable.OnSubscribe<Pair>).subscribeOn(Schedulers.io()).subscribe({ pair ->
            def items=pair.key
            if(pair.key) {
                planItems += items
                Platform.runLater({ items.each { addTaskPlan(it) } })
            }
            !pair.value?:initAddButton()
        }, { it.printStackTrace() })
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
        planLabel.setText("任务计划:$item.name")
        Label caseLabel=child.lookup("#caseLabel")
        caseLabel.setText("执行用例:$item.testCase")

        Label startTimeLabel=child.lookup("#startTimeLabel")
        startTimeLabel.setText("起始时间:$item.startDate")
        Label endTimeLabel=child.lookup("#endTimeLabel")
        endTimeLabel.setText("结束时间:$item.endDate")
        Label cycleLabel=child.lookup("#cycleLabel")
        cycleLabel.setText("循环执行:${item.cycle?"YES":"NO"}")
        masonryPane.children.add(child)
    }

}
