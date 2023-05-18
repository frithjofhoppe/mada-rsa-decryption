module com.example.mada {

    requires org.controlsfx.controls;
    requires javafx.fxml;

    exports com.example.mada.huffmann;
    opens com.example.mada.huffmann to javafx.fxml;

    exports com.example.mada.rsa;
    opens com.example.mada.rsa to javafx.fxml;

    exports com.example.mada.elgamal;
    opens com.example.mada.elgamal to javafx.fxml;
}
