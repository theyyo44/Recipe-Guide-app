package sample.yemek;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Tabloları oluştur
        DatabaseHelper.createTables();

        // JavaFX UI başlatma
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1250, 720);

        stage.setTitle("Yemek Tarifleri Uygulaması");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // JavaFX uygulamasını başlat
        launch();

    }
}
