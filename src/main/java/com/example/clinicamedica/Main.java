package com.example.clinicamedica;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    public void start(Stage primaryStage) {
        InterfaceJavaFx mainApp = new InterfaceJavaFx();
        mainApp.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}