package quant.test.server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import quant.test.server.prefs.PrefsKey;
import quant.test.server.prefs.SharedPrefs;
import quant.test.server.util.TextUtils;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        Scene scene;
        String value = SharedPrefs.get(PrefsKey.ADB);
        if(TextUtils.isEmpty(value)){
            //初始化配置
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/prefs_adb_layout.fxml"));
            primaryStage.setTitle("Hello!");
            scene = new Scene(root, 520, 640);
            primaryStage.setScene(scene);
        } else {
            Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/main_layout.fxml"));
            primaryStage.setTitle("调试程序");
            scene = new Scene(root, 720, 600);
            primaryStage.setScene(scene);
        }
        scene.getStylesheets().add(getClass().getResource("/resources/css/jfoenix-fonts.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/resources/css/jfoenix-design.css").toExternalForm());
        scene.getStylesheets().add(getClass().getClassLoader().getResource("css/jfoenix-main-demo.css").toExternalForm());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
