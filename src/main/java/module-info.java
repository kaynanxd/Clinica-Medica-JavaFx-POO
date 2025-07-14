module com.example.clinicamedica {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.google.zxing;
    requires com.google.zxing.javase;


    opens com.example.clinicamedica to javafx.fxml;
    exports com.example.clinicamedica;
}