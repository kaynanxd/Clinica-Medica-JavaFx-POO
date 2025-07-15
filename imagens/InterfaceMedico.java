package com.example.clinicamedica.interfaces;

import com.example.clinicamedica.UsuarioMedico;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class InterfaceMedico {

    private final InterfaceFacade facade;

    public InterfaceMedico(InterfaceFacade facade) {
        this.facade = facade;
    }

    public void exibirMenuMedico(Stage primaryStage, UsuarioMedico medico) {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);

        Text titulo = new Text("Menu do Médico");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Button voltar = new Button("Voltar para Início");
        voltar.setOnAction(e -> facade.iniciarAplicacao());

        layout.getChildren().addAll(titulo, voltar);
        Scene cena = new Scene(layout, 600, 400);
        primaryStage.setScene(cena);
        primaryStage.setTitle("Médico");
        primaryStage.show();
    }
}