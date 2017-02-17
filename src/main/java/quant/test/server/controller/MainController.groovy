package quant.test.server.controller
import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import com.jfoenix.controls.JFXListView
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import quant.test.server.animation.PaneTransition
import quant.test.server.anntation.FXMLLayout
import quant.test.server.bus.RxBus
import quant.test.server.event.OnDeviceConnectedEvent
import quant.test.server.exception.ExceptionHandler
import quant.test.server.log.Log
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
    static String TAG="MainController"
    final def executorService=Executors.newCachedThreadPool()
    @FXML
    StackPane contentPane
    @FXML
    JFXListView deviceList
    @FXML
    Label buttonDeviceInfo
    @FXML
    Label buttonMessage

    def serverSocket
    def cachePane=[:]
    int lastIndex
    @Override
    void initialize(URL location, ResourceBundle resources) {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler())
        deviceList.setItems(FXCollections.observableArrayList())
        deviceList.setCellFactory({new DeviceListCell()})


        bindPane(0,buttonDeviceInfo,DeviceInfoController.class,true,true)
        bindPane(1,buttonMessage,MessageController.class,true,false)
        //初始化调试桥
        initBridge(SharedPrefs.get(PrefsKey.ADB))
    }

    /**
     * 绑定panel
     * @param fxml
     * @param attach
     */
    def bindPane(int index,Control control,Class clazz,boolean attach,boolean defaultPane) {
        if(attach&&!cachePane[index]){
            ensurePane(clazz, index,defaultPane)
        }
        control.setOnMouseClicked({
            ensurePane(clazz,index,defaultPane)
            def currentPane=cachePane[index]
            def lastPane=cachePane[lastIndex]
            new PaneTransition(currentPane,lastPane,index<lastIndex?-contentPane.width:contentPane.width).start()
            lastIndex=index
        })
    }

    private void ensurePane(Class clazz, int index,boolean defaultPane) {
        def fxml
        def annotation = clazz.getAnnotation(FXMLLayout.class)
        !annotation?:(fxml=annotation.value())
        if(!fxml){
            throw new IllegalArgumentException("must use controller and config FXMLLayout!")
        } else if(!cachePane[index]){
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(fxml))
            Pane parent = loader.load()
            parent.setVisible(defaultPane)
            contentPane.children.add(parent)
            cachePane << [(index): parent]
        }
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
            Log.e(TAG,"打开 Socket 服务,端口:5556!")
            while (socket = serverSocket.accept()) {
                String hostAddress = socket.getInetAddress().getHostAddress();
                Log.e(TAG,"连接一个远程设备:" + hostAddress)
                executorService.execute(new ClientService(hostAddress,socket,bridge))
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
                Log.e(TAG,"设备连接中断:${iDevice.serialNumber}($iDevice.state)")
            }

            @Override
            public void deviceDisconnected(IDevice iDevice) {
                //设备中断
                if (null != iDevice) {
                    Platform.runLater({deviceList.getItems().remove(DeviceItem.form(iDevice))})
                }
            }

            @Override
            public void deviceChanged(IDevice iDevice, int i) {
                if(null!=iDevice&&IDevice.CHANGE_BUILD_INFO==i){
                    if(iDevice.serialNumber.equals(iDevice.properties["gsm.serial"])){
                        //代表为有线连接
                    } else {
                        //代表为无线连接
                    }
                    def deviceItem=DeviceItem.form(iDevice)
                    RxBus.post(new OnDeviceConnectedEvent(deviceItem))
                    Platform.runLater({deviceList.getItems().add(deviceItem)})
                    Log.e(TAG,"设备己连接:${iDevice} state:$i device-state:$iDevice.state")
                }
            }
        });
        executorService.execute({
            final AndroidDebugBridge bridge = AndroidDebugBridge.createBridge(adbPath as String, false)
            waitDevicesList(bridge)
            //创造服务socket
            connectSocket(bridge)
        })
    }


}
