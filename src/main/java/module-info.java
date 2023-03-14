module com.example.mada_rsa_project_2 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens com.example.mada_rsa_project_2 to javafx.fxml;
    exports com.example.mada_rsa_project_2;
}
