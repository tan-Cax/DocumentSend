package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.AppConfig;

public class App extends Application {

    private static int initialPort = AppConfig.getListenPort();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("DocumentSend - " + AppConfig.getUsername());
        primaryStage.setScene(new Scene(new MainLayout(), 800, 500));
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
    }

    public static void setPort(int port) {
        initialPort = port;
    }
}
