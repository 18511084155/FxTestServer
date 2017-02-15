package quant.test.server.controller

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import com.jfoenix.controls.JFXListView
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.Initializable
import quant.test.server.exception.ExceptionHandler
import quant.test.server.model.DeviceItem
import quant.test.server.service.ClientService
import quant.test.server.widget.DeviceListCell

import java.util.concurrent.Executors

/**
 * Created by Administrator on 2017/2/15.
 */
class MainController implements Initializable{
    @FXML
    private JFXListView deviceList
    final def executorService
    def serverSocket
    @Override
    void initialize(URL location, ResourceBundle resources) {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler())
        deviceList.setItems(FXCollections.observableArrayList())
        deviceList.setCellFactory({new DeviceListCell()})
        //初始化调试桥
        initBridge()
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
                    if(iDevice.serialNumber.equals(iDevice.properties["gsm.serial"])){
                        //代表为有线连接
                    } else {
                        //代表为无线连接
                    }
                    deviceList.getItems().add(DeviceItem.form(iDevice))
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

}
