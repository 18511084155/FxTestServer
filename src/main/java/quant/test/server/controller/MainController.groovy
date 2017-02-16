package quant.test.server.controller
import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import com.jfoenix.controls.JFXDrawer
import com.jfoenix.controls.JFXListView
import io.datafx.controller.flow.Flow
import io.datafx.controller.flow.FlowHandler
import io.datafx.controller.flow.container.AnimatedFlowContainer
import io.datafx.controller.flow.container.ContainerAnimations
import io.datafx.controller.flow.context.FXMLViewFlowContext
import io.datafx.controller.flow.context.ViewFlowContext
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.util.Duration
import quant.test.server.bus.RxBus
import quant.test.server.event.OnDeviceConnectedEvent
import quant.test.server.exception.ExceptionHandler
import quant.test.server.model.DeviceItem
import quant.test.server.prefs.PrefsKey
import quant.test.server.prefs.SharedPrefs
import quant.test.server.service.ClientService
import quant.test.server.widget.DeviceListCell

import java.util.concurrent.Executors
/**
 * Created by Administrator on 2017/2/15.
 */
class MainController implements Initializable{
    @FXMLViewFlowContext
    ViewFlowContext context
    @FXML
    JFXDrawer drawer
    @FXML
    Label button1
    @FXML
    Label button2

    FlowHandler flowHandler
    @FXML
    JFXListView deviceList
    final def executorService
    def serverSocket
    @Override
    void initialize(URL location, ResourceBundle resources) {
        // create the inner flow and content
        context = new ViewFlowContext();
        // set the default controller
        Flow innerFlow = new Flow(DeviceInfoController.class);

        flowHandler = innerFlow.createHandler(context);
        context.register("ContentFlowHandler", flowHandler);
        context.register("ContentFlow", innerFlow);
        drawer.setContent(flowHandler.start(new AnimatedFlowContainer(Duration.millis(320), ContainerAnimations.SWIPE_LEFT)));
        context.register("ContentPane", drawer.getContent().get(0));

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler())
        deviceList.setItems(FXCollections.observableArrayList())
        deviceList.setCellFactory({new DeviceListCell()})

        deviceList.depthProperty().set(4)

        bindNodeToController(button1,DeviceInfoController.class,innerFlow)

        //初始化调试桥
        initBridge(SharedPrefs.get(PrefsKey.ADB))
    }

    /**
     * 在10秒内未取得设备代表连接异常
     *
     * @param bridge
     */
    boolean waitDevicesList(AndroidDebugBridge bridge) {
        int count = 0;
        boolean result = true;
        while (!bridge.hasInitialDeviceList()) {
            if (count++ > 20) {
                result = false;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 初始化socket
     */
    def connectSocket(bridge) {
        //这里检测到端口已经被占用,干掉进程
//        Command.shell("lsof -i tcp:5556 | grep -v PID | awk '{kill \$2}'")
        try {
            Socket socket
            serverSocket = new ServerSocket(5556);
//            messageTextArea.append("打开 Socket 服务,端口:5556!\n")
            while (socket = serverSocket.accept()) {
                String hostAddress = socket.getInetAddress().getHostAddress();
//                messageTextArea.append("连接一个远程设备::" + hostAddress+"\n")
                ClientService clientService = new ClientService(hostAddress,socket,bridge)
//                clientService.printMessage{ messageTextArea.append(it+"\n") }
                executorService.execute(clientService);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 初始化调试桥
     */
    def initBridge(adbPath) {
        AndroidDebugBridge.init(false);
        AndroidDebugBridge.addDeviceChangeListener(new AndroidDebugBridge.IDeviceChangeListener() {
            @Override
            public void deviceConnected(IDevice iDevice) {
                println "DeviceConnected:${iDevice.serialNumber}($iDevice.state)";
            }

            @Override
            public void deviceDisconnected(IDevice iDevice) {
                //设备中断
                if (null != iDevice) {
                    deviceList.getItems().remove(DeviceItem.form(iDevice))
                }
            }

            @Override
            public void deviceChanged(IDevice iDevice, int i) {
                if(null!=iDevice&&IDevice.CHANGE_BUILD_INFO==i){
                    println iDevice.serialNumber
                    if(iDevice.serialNumber.equals(iDevice.properties["gsm.serial"])){
                        //代表为有线连接
                    } else {
                        //代表为无线连接
                    }
                    def deviceItem=DeviceItem.form(iDevice)
                    deviceList.getItems().add(deviceItem)
                    RxBus.post(new OnDeviceConnectedEvent(deviceItem))
                }
                println "deviceChanged:${iDevice} state:$i device-state:$iDevice.state";
            }
        });
        Executors.newSingleThreadExecutor().execute({
            final AndroidDebugBridge bridge = AndroidDebugBridge.createBridge(adbPath as String, false)
            waitDevicesList(bridge)
            //创造服务socket
            connectSocket(bridge)
        })
    }

    def bindNodeToController(Node node, Class<?> controllerClass, Flow flow) {
        flow.withGlobalLink(node.getId(), controllerClass);
        node.setOnMouseClicked({
            flowHandler.handle(node.id)
            int selectedIndex=deviceList.getSelectionModel().selectedIndex
            //切换面板后,仍然发送当前事件,否则会造成面板
            RxBus.post(new OnDeviceConnectedEvent(deviceList.items.get(0>selectedIndex?0:selectedIndex)))
        })
    }

}
