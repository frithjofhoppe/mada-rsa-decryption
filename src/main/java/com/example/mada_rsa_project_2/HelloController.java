package com.example.mada_rsa_project_2;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class HelloController {

    @FXML
    private Label welcomeText;

    private Stage stage;

    @FXML
    protected void uploadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Text File to encrypt");
        fileChooser.showOpenDialog(stage);

    }

    @FXML
    protected void generateRsaKeyPair() {
        //TODO
        System.out.println("test");
    }
}
