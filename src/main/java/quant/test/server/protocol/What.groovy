package quant.test.server.protocol

/**
 * Created by czz on 2017/2/3.
 * 客户端信息通信
 * 1:操作信息
 *  1.1:控制adb连接/中断连接某个客户端
 *      1.1.1:控制 adb 连接,分为有线/无线 有线,则检测 status,若为 offline->弹出弹窗,提示用户授权,若为无线,且 root,自动关联
 *  1.2:添加删除测试用例
 * 2:交互操作
 *  2.1:远程操作所有客户端,如启动测试,暂停,选择测试用例,执行.获取测试结果,时时查看操作信息等
 * 3:文件传递
 *  3.1:远程获取测试报告,查看异常日志
 *
 *
 */
class What {
    static class TASK {
        final static int TYPE_LOG=1
        final static int TYPE_INIT_PID=2
        final static int TYPE_RUN_COMPLETE=3
        final static int TYPE_RUN_LOOP=4
        final static int TYPE_RUN_RESULT=5
    }

    static class INSTALL{
        final static int TYPE_LOG=1
        final static int TYPE_INIT_PID=2
        final static int TYPE_INSTALL_SUCCESS=3
        final static int TYPE_INSTALL_CHECK=4
    }

    static class ADB{
        static final def ID= ADB.class.simpleName.hashCode()
        static final def CHECK_ADB=ID+1
        static final def OFFLINE=ID+2
        static final def CONNECT=ID+3
        static final def SET_PORT=ID+4
        static final def CONNECT_COMPLETE=ID+5
        static final def CONNECT_FAILED=ID+6
        static final def KILL_SERVER=ID+7
        static final def START_SERVER=ID+8
        static final def LOG=ID+9
        static final def ADB_INTERRUPT=ID+10
        static final int ALERT_ADB_DEBUG=ID+11;//连上socket,但未root,未连接usb
    }
}
