module com.example.mada_rsa_project_2 {

    requires org.controlsfx.controls;
    requires javafx.fxml;

    opens com.example.mada to javafx.fxml;
    exports com.example.mada.huffmann;
    opens com.example.mada.huffmann to javafx.fxml;
    exports com.example.mada.rsa;
    opens com.example.mada.rsa to javafx.fxml;
}
