package quant.test.server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import quant.test.server.prefs.PrefsKey;
import quant.test.server.prefs.SharedPrefs;
import quant.test.server.util.TextUtils;

import java.io.File;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{

        String value = SharedPrefs.get(PrefsKey.ADB);
        StageManager stageManager = StageManager.getInstance();
        if(TextUtils.isEmpty(value)||!new File(value).exists()){
//            //初始化配置
            primaryStage.setTitle("Hello!");
            stageManager.stage(primaryStage,getClass().getClassLoader().getResource("fxml/prefs_adb_layout.fxml"), 520, 640);
        } else {
            //主界面
            primaryStage.setTitle("调试程序");
            stageManager.stage(primaryStage,getClass().getClassLoader().getResource("fxml/main_layout.fxml"),840,720);
        }
        primaryStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
