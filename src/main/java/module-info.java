module com.example.mada_rsa_project_2 {

    requires org.controlsfx.controls;
    requires javafx.fxml;

    opens com.example.mada_rsa_project_2 to javafx.fxml;
    exports com.example.mada_rsa_project_2;
}
