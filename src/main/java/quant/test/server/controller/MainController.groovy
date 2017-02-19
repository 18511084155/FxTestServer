package quant.test.server.controller
import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import com.jfoenix.controls.JFXListView
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.control.Toggle
import javafx.scene.control.ToggleButton
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import quant.test.server.animation.PaneTransition
import quant.test.server.anntation.FXMLLayout
import quant.test.server.bus.RxBus
import quant.test.server.event.OnDeviceSelectedEvent
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
    HBox tabLayout
    @FXML
    JFXListView deviceList
    @FXML
    ToggleButton buttonDeviceInfo
    @FXML
    ToggleButton buttonTask
    @FXML
    ToggleButton buttonTest
    @FXML
    ToggleButton buttonDoc
    @FXML
    ToggleButton buttonMessage

    def serverSocket
    def cachePane=[:]
    @Override
    void initialize(URL location, ResourceBundle resources) {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler())
        deviceList.setItems(FXCollections.observableArrayList())
        deviceList.setCellFactory({new DeviceListCell()})
        deviceList.setOnMouseClicked({
            def selectedItem=deviceList.getSelectionModel().getSelectedItem()
            if(selectedItem){
                RxBus.post(new OnDeviceSelectedEvent(selectedItem as DeviceItem))
            }
        })

        ToggleGroup toggleGroup=new ToggleGroup()
        buttonDeviceInfo.setToggleGroup(toggleGroup)
        buttonTask.setToggleGroup(toggleGroup)
        buttonTest.setToggleGroup(toggleGroup)
        buttonDoc.setToggleGroup(toggleGroup)
        buttonMessage.setToggleGroup(toggleGroup)

        toggleGroup.selectToggle(buttonDeviceInfo)
        final def controllerArray=[DeviceInfoController.class,TaskController.class,TestCaseController.class,TestDocController.class,MessageController.class]
        def listener={ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle selectedToggle->
            int index=tabLayout.children.indexOf(selectedToggle)
            def oldIndex = tabLayout.children.indexOf(oldValue)
            -1!=index?: oldValue.setSelected(true)
            if(-1!=index&&-1!=oldIndex&&oldIndex!=index){
                loadPane(controllerArray[index],index,false)
                new PaneTransition(cachePane[oldIndex],cachePane[index],index>oldIndex?-contentPane.width:contentPane.width).start()
            }
        } as ChangeListener
        toggleGroup.selectedToggleProperty().addListener(listener)
        //装载默认面板
        loadPane(controllerArray[0],0,true)
        //装载消息面板
        loadPane(controllerArray[4],4,false)
        //初始化调试桥
        initBridge(SharedPrefs.get(PrefsKey.ADB))
    }


    private void loadPane(Class clazz, int index, boolean defaultPane) {
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
            }

            @Override
            public void deviceDisconnected(IDevice iDevice) {
                //设备中断
                if (null != iDevice) {
                    Log.e(TAG,"设备连接中断:${iDevice.serialNumber}($iDevice.state)")
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
                    Platform.runLater({
                        def deviceItem=DeviceItem.form(iDevice)
                        deviceList.getItems().add(deviceItem)
                        if(1==deviceList.items.size()){
                            //首次信息初始化
                            deviceList.selectionModel.select(0)
                            RxBus.post(new OnDeviceSelectedEvent(deviceItem))
                        }
                    })
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
