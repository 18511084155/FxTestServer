package quant.test.server;

import com.sun.javafx.application.PlatformImpl;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import quant.test.server.log.Log;
import quant.test.server.prefs.PrefsKey;
import quant.test.server.prefs.SharedPrefs;
import quant.test.server.util.TextUtils;

import java.io.File;

/**
 * persist.service.bdroid.bdaddress 型号相同
 */
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        Log.startProcess();//启动日志线程
        String value = SharedPrefs.get(PrefsKey.ADB);
        StageManager stageManager = StageManager.getInstance();
        if(TextUtils.isEmpty(value)||!new File(value).exists()){
//            //初始化配置
            primaryStage.setTitle("Hello!");
            stageManager.stage(primaryStage,getClass().getClassLoader().getResource("fxml/prefs_adb_layout.fxml"), 520, 640);
        } else {
            //主界面
            primaryStage.setTitle("调试程序");
            stageManager.stage(primaryStage, getClass().getClassLoader().getResource("fxml/main_layout.fxml"), 900, 720);
        }

//        stageManager.stage(primaryStage, getClass().getClassLoader().getResource("fxml/add_task_layout.fxml"), 900, 720);

        //结束监听
        PlatformImpl.addListener(new PlatformImpl.FinishListener() {
            @Override
            public void idle(boolean implicitExit) {
            }

            @Override
            public void exitCalled() {
                //exit
                System.exit(0);
            }
        });
        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();

        });
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
