package sgu.ltudm.songssingersproject;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sgu.ltudm.songssingersproject.Helpers.MyAlert;
import sgu.ltudm.songssingersproject.models.Client;
import sgu.ltudm.songssingersproject.models.Model;

import java.io.IOException;

public class App extends Application {

    public static void main(String[] args) {
        try {
            launch();
        } catch (Exception e) {
            MyAlert alert = new MyAlert(e.getMessage(), Alert.AlertType.ERROR);
            alert.show();
        }
    }
    @Override
    public void start(Stage primaryStage) {
        try {
            Client.connectServer();

            // Bắt sự kiện khi ứng dụng đóng
            primaryStage.setOnCloseRequest(this::handleClose);

            Model.getInstance().getView().showAppWindow();
            Model.getInstance().getView().getAppSelectedMenuItem().set("Home");
        } catch (Exception e) {
            MyAlert alert = new MyAlert(e.getMessage(), Alert.AlertType.ERROR);
            alert.show();
        }
    }

    private void handleClose(WindowEvent event) {
        // Đóng server trước khi thoát
        try {
            Client.stop(); // Giả sử bạn có một phương thức stop trong lớp Server
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Thoát ứng dụng
        Platform.exit();
        System.exit(0);
    }
}