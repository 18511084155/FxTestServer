package quant.test.server.service
import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import groovy.json.JsonSlurper
import quant.test.server.bus.RxBus
import quant.test.server.command.Command
import quant.test.server.event.OnDeviceAdbConnectedEvent
import quant.test.server.log.Log
import quant.test.server.model.DeviceItem
import quant.test.server.model.Property
import quant.test.server.protocol.Json
import quant.test.server.protocol.What
/**
 * Created by czz on 2017/2/3.
 * 客户端服务对象
 */
class ClientService implements Runnable,AndroidDebugBridge.IDeviceChangeListener{
    final static String TAG="ClientService"
    final PrintWriter printWriter
    final AndroidDebugBridge bridge
    final Socket socket
    final def address
    def reader

    ClientService(address, socket, AndroidDebugBridge bridge) {
        this.address=address
        this.socket = socket
        this.bridge = bridge
        this.printWriter = new PrintWriter(socket.getOutputStream());
    }

    @Override
    void run() {
        //等待 socket 信息
        checkAdbConnect(address)
        waitSocketMessage()
    }

    /**
     * 等待 socket 信息
     */
    private void waitSocketMessage() {
        try {
            def line
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            while (null!=reader&&(line = reader.readLine()) != null) {
                def item
                try{
                    item = new JsonSlurper().parseText(line)
                } catch (Exception e){
                    printf "message:$line 解析失败!"
                }
                if (item) {
                    if (What.ADB.CONNECT == item.what) {
                        connectDevice(item.address)
                    }
                }
            }
        } catch (Exception e) {
            //socket中断,设备失去连接
            e.printStackTrace();
        } finally {
            closeSocket()
        }
    }

    /**
     * 连接设备,检测为无线连接,或是有线,无线需root,有线则直接连接
     * @param address
     */
    def connectDevice(String address) {
        println "开始连接设备:" + address
        Command.shell("adb connect $address:5555",null){
            if(it){
                //connected to 192.168.100.201:5555
                def matcher=it.endsWith("connected to ${address}:5555") as String
                if(matcher){
                    //连接成功?
                    println "连接成功:$address"
                } else {
                    //重联
                    connectDevice(address)
                    println "连接失败:$address 开始重联"
                }
            }
        }
    }


    /**
     * 检测adb是否连接上
     * 1:无线,但是未连上
     * 2:有线,连上但是未显示,或者 offline 状态,或者未连上 adb
     *861322030453548
     */
    def checkAdbConnect(address) {
        //检测机器是否连接
        def deviceItem=[]
        sendMessage(What.ADB.LOG,address,"开始连接Adb:$address")
        devices?.each {
            if (address?.equals(it.properties[Property.DEVICE_ADDRESS])) {
                deviceItem<<DeviceItem.form(it)
            }
        }
        //如果未连接.尝试连接
        if(deviceItem){
            //连接己连接成功,根据状态状态设备设备是否需要尝试重联,如 offline
            Log.e(TAG,"设备:${address} Adb己连接")
            sendMessage(What.ADB.CONNECT_COMPLETE,address,null)
            sendMessage(What.ADB.LOG,address,"设备:${address} Adb己连接")
            RxBus.post(new OnDeviceAdbConnectedEvent(deviceItem[0]))
        } else {
            //说明,未插入设备,通知其检测是否 root,如果未 root 提示其插入 usb
            sendMessage(What.ADB.CHECK_ADB,address,null)
        }
        //添加设备监听,为此 socket 连接设备动态更新状态
        AndroidDebugBridge.removeDeviceChangeListener(this);
        AndroidDebugBridge.addDeviceChangeListener(this);
    }

    @Override
    public void deviceConnected(IDevice iDevice) {
    }

    @Override
    public void deviceDisconnected(IDevice iDevice) {
        //设备中断,查看设备为无线连接,或是有线,若为无线,则尝试重联,有线
        if (null != iDevice) {
            def deviceAddress=iDevice.properties[Property.DEVICE_ADDRESS]
            if (address.equals(deviceAddress)) {
                //当前设备中断,设置重连
                sendMessage(What.ADB.ADB_INTERRUPT,address,null)//设备adb中断
                sendMessage(What.ADB.LOG,address,"设备:${iDevice.serialNumber} 中断,尝试重联...")
                checkAdbConnect(address)
            }
        }
    }

    @Override
    public void deviceChanged(IDevice iDevice, int i) {
        println "iDevice:$address i:$i"
        if(null!=iDevice&&IDevice.CHANGE_BUILD_INFO==i){
            String deviceAddress = iDevice.properties[Property.DEVICE_ADDRESS]
            println "设备己连接:$address deviceAddress:$deviceAddress"
            if(address.equals(deviceAddress)){
                //设备连接
                sendMessage(What.ADB.CONNECT_COMPLETE,deviceAddress,null)//设备adb
                sendMessage(What.ADB.LOG,address,"设备:${deviceAddress} Adb己连接")
                RxBus.post(new OnDeviceAdbConnectedEvent(DeviceItem.form(iDevice)))
            }
        }
    }

    /**
     * 向客户端发送消息
     *
     * @param message
     */
    def sendMessage(what,address,message) {
        if (printWriter) {
            printWriter.println(Json.map2(["what":what, "address":address, "message":message?:""]))
            printWriter.flush()
        }
    }


    /**
     * 获取连接设备
     *
     * @return
     */
    private IDevice[] getDevices() {
        IDevice[] devices = null;
        if (waitDevicesList(bridge)) {
            devices = bridge.getDevices();
        }
        return devices;
    }

    /**
     * 在10秒内未取得设备代表连接异常
     * @param bridge
     */
    def waitDevicesList(bridge) {
        int count = 0;
        boolean result = true;
        while (!bridge.hasInitialDeviceList()) {
            if (count++ > 20) {
                Log.e(TAG,"初始化连接设备列表超时!")
                result = false
            }
            Thread.sleep(1000);
        }
        return result;
    }

    /**
     * 判断是否断开连接
     *
     * @return
     */
    def socketIsConnect() {
        boolean result = null!=socket;
        try {
            if(socket) socket.sendUrgentData(0)
        } catch (Exception se) {
            result = false;
        }
        return result;
    }


    /**
     * 关闭socket
     */
    def closeSocket() {
        try {
            if (null != socket && !socket.isClosed()) {
                printWriter.close();
                socket.close();
            }
            reader?.close()
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
