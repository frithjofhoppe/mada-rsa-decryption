package com.example.mada.huffmann;

import com.example.mada.rsa.RSAApplication;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HuffmannApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        var os = System.getProperty("os.name");
        System.out.println(os);
        var filePath = os.contains("Windows") ? "/com/example/mada/huffmann.fxml" : "\\com\\example\\mada\\huffmann.fxml";
        FXMLLoader fxmlLoader = new FXMLLoader(RSAApplication.class.getResource(filePath));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Huffmann Util");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}
