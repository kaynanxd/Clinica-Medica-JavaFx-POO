
package com.example.clinicamedica;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.clinicamedica.RealizarConsulta.gerarBoletoComEspecialidadePreco;

public class MainJavaFX extends Application {

    private Stage primaryStage;
    private Usuario usuarioLogado = null; // armazena o usuário logado

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("Hygiea Health");
        primaryStage.setResizable(false);
        mostrarTelaLogin();
    }


    private void mostrarTelaLogin() {
        VBox loginForm = new VBox(15);
        loginForm.setAlignment(Pos.CENTER);
        loginForm.setPadding(new Insets(20));

        Label loginLabel = new Label("Login");
        loginLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        TextField idField = new TextField();
        idField.setPromptText("ID ou Nome:");
        idField.setMaxWidth(Double.MAX_VALUE);
        idField.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");

        PasswordField senhaField = new PasswordField();
        senhaField.setPromptText("Senha:");
        senhaField.setMaxWidth(Double.MAX_VALUE);
        senhaField.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");

        Button entrarButton = new Button("ENTRAR");
        entrarButton.setMaxWidth(Double.MAX_VALUE);
        entrarButton.setStyle("-fx-background-color: linear-gradient(to right, #66e2d4, #3fd0c9); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-border-radius: 20;");
        entrarButton.setOnAction(e -> {
            GerenciadorLogin login = new GerenciadorLogin();
            Usuario usuario = login.verificarCredenciais(idField.getText(), senhaField.getText());
            if (usuario != null) {
                usuarioLogado = usuario; // Armazena o usuário logado
                mostrarTelaPrincipal(usuario);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Credenciais inválidas.");
                alert.showAndWait();
            }
        });

        Hyperlink criarContaLink = new Hyperlink("Criar Nova Conta");
        criarContaLink.setOnAction(e -> mostrarTelaCriarConta());

        loginForm.getChildren().addAll(loginLabel, idField, senhaField, entrarButton, criarContaLink);

        StackPane root = montarLayoutBase(loginForm);
        Scene cena = new Scene(root, 800, 500);
        primaryStage.setScene(cena);
        primaryStage.show();
    }

    private void mostrarTelaCriarConta() {
        VBox criarContaForm = new VBox(15);
        criarContaForm.setAlignment(Pos.CENTER);
        criarContaForm.setPadding(new Insets(20));

        Label criarContaLabel = new Label("Criar Conta");
        criarContaLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        ToggleGroup grupoTipo = new ToggleGroup();
        RadioButton medicoBtn = new RadioButton("Médico");
        RadioButton pacienteBtn = new RadioButton("Paciente");
        medicoBtn.setToggleGroup(grupoTipo);
        pacienteBtn.setToggleGroup(grupoTipo);
        pacienteBtn.setSelected(true);
        HBox tipoUsuario = new HBox(10, medicoBtn, pacienteBtn);

        TextField nomeField = new TextField();
        nomeField.setPromptText("Nome:");
        nomeField.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");

        PasswordField senhaField = new PasswordField();
        senhaField.setPromptText("Senha:");
        senhaField.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");

        TextField idadeOuEspecialidadeField = new TextField();
        idadeOuEspecialidadeField.setPromptText("Idade:");
        idadeOuEspecialidadeField.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");

        TextField planoOuPlanosField = new TextField();
        planoOuPlanosField.setPromptText("Plano de saúde (ou 'Sem'):");
        planoOuPlanosField.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");

        grupoTipo.selectedToggleProperty().addListener((obs, old, novo) -> {
            if (grupoTipo.getSelectedToggle() == medicoBtn) {
                idadeOuEspecialidadeField.setPromptText("Especialidade:");
                planoOuPlanosField.setPromptText("Planos de saúde (ex: Unimed,pix):");
            } else {
                idadeOuEspecialidadeField.setPromptText("Idade:");
                planoOuPlanosField.setPromptText("Plano de saúde ou 'sem'):");
            }
        });

        Button criarButton = new Button("CRIAR");
        criarButton.setMaxWidth(Double.MAX_VALUE);
        criarButton.setStyle("-fx-background-color: linear-gradient(to right, #66e2d4, #3fd0c9); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-border-radius: 20;");
        criarButton.setOnAction(e -> {
            String nome = nomeField.getText();
            String senha = senhaField.getText();
            Usuario novoUsuario = null;

            if (grupoTipo.getSelectedToggle() == medicoBtn) {
                String especialidade = idadeOuEspecialidadeField.getText();
                List<String> planos = Arrays.stream(planoOuPlanosField.getText().split(","))
                        .map(String::trim).toList();
                novoUsuario = UsuarioFactory.criarMedico(senha, nome, especialidade, planos);
            } else {
                int idade = Integer.parseInt(idadeOuEspecialidadeField.getText());
                String plano = planoOuPlanosField.getText();
                novoUsuario = UsuarioFactory.criarPaciente(senha, nome, idade, plano);
            }

            if (novoUsuario != null) {
                EscreverArquivo.escreverDadosUsuario(novoUsuario);
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Usuário criado com sucesso!");
                alert.showAndWait();
                mostrarTelaLogin();
            }
        });

        Hyperlink voltarLoginLink = new Hyperlink("Voltar para Login");
        voltarLoginLink.setOnAction(e -> mostrarTelaLogin());

        criarContaForm.getChildren().addAll(
                criarContaLabel, tipoUsuario, nomeField, senhaField,
                idadeOuEspecialidadeField, planoOuPlanosField, criarButton, voltarLoginLink
        );

        StackPane root = montarLayoutBase(criarContaForm);
        Scene cena = new Scene(root, 800 , 500 );
        primaryStage.setScene(cena);
        primaryStage.show();
    }

    private void mostrarTelaPrincipal(Usuario usuario) {
        if (usuario instanceof UsuarioPaciente) {
            mostrarMenuPaciente((UsuarioPaciente) usuario);
        } else if (usuario instanceof UsuarioMedico) {
            mostrarMenuMedico((UsuarioMedico) usuario);
        }
    }

    private void mostrarMenuMedico(UsuarioMedico medico) {

        StackPane background = new StackPane();
        background.setStyle("-fx-background-color: linear-gradient(to top, #3fd0c9,#7FFFD4 ); -fx-background-radius: 30;");
        background.setPadding(new Insets(20));


        VBox PainelConteudo = new VBox(20);
        PainelConteudo.setAlignment(Pos.TOP_CENTER);
        PainelConteudo.setPadding(new Insets(30, 30, 30, 30));
        PainelConteudo.setStyle("-fx-background-color: #f8fefc; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0.5, 0, 0);");
        PainelConteudo.setMaxWidth(700);
        PainelConteudo.setMaxHeight(400);

        // barra no topo
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: #66e2d4; " +
                "-fx-background-radius: 20 20 20 20;"); //arredondar bordas

        // parte da logo
        HBox leftTopBar = new HBox(5);
        leftTopBar.setAlignment(Pos.CENTER_LEFT);
        StackPane logoContainer = new StackPane();
        logoContainer.setPrefSize(30, 30);
        logoContainer.setStyle("-fx-background-color: #fff; -fx-background-radius: 15;");
        Region logoCircle = new Region();
        logoCircle.setPrefSize(20, 20);
        logoCircle.setStyle("-fx-background-color: #66e2d4; -fx-background-radius: 10;");
        logoContainer.getChildren().add(logoCircle);
        Label hygieaLabel = new Label("Hygiea Health");
        hygieaLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        hygieaLabel.setTextFill(Color.WHITE);
        leftTopBar.getChildren().addAll(logoContainer, hygieaLabel);
        HBox.setHgrow(leftTopBar, Priority.ALWAYS);

        // icone do usuario e botao de sair
        HBox rightTopBar = new HBox(5);
        rightTopBar.setAlignment(Pos.CENTER_RIGHT);
        Label nomeUsuarioLabel = new Label(medico.getNome());
        nomeUsuarioLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        nomeUsuarioLabel.setTextFill(Color.WHITE);
        StackPane userIconContainer = new StackPane();
        userIconContainer.setPrefSize(30, 30);
        userIconContainer.setStyle("-fx-background-color: #fff; -fx-background-radius: 15;");
        Label userIcon = new Label("\uD83D\uDC64");
        userIcon.setFont(Font.font("Arial", 18));
        userIconContainer.getChildren().add(userIcon);
        Button sairBtn = new Button("Sair");
        sairBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold;");
        sairBtn.setOnAction(e -> mostrarTelaLogin());

        rightTopBar.getChildren().addAll(nomeUsuarioLabel, userIconContainer, sairBtn);

        topBar.getChildren().addAll(leftTopBar, rightTopBar);
        VBox.setMargin(topBar, new Insets(0,0,10,0));

        Label welcomeLabel = new Label("Bem-Vindo!");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        welcomeLabel.setPadding(new Insets(20, 0, 20, 0));

        // grid para as opcoes
        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(30);
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(0, 0, 0, 0)); // Padding ajustado

        Button visualizarAgendamentosBtn = criarBotaoMenu("\uD83D\uDC41", "Visualizar meus Agendamentos");
        visualizarAgendamentosBtn.setOnAction(e -> {
            mostrarTelaVisualizarAgendamentos(medico);
        });

        Button realizarConsultaBtn = criarBotaoMenu("\uD83D\uDC68\u200D⚕️➕", "Realizar Consulta");
        realizarConsultaBtn.setOnAction(e -> {
            mostrarTelaSelecionarConsultaParaRealizar(medico);
        });

        Button alterarDadosBtn = criarBotaoMenu("\uD83D\uDCBE", "Alterar Dados");
        alterarDadosBtn.setOnAction(e -> {
            mostrarTelaAlterarDados(medico);
        });

        grid.add(visualizarAgendamentosBtn, 0, 0);
        grid.add(realizarConsultaBtn, 1, 0);
        grid.add(alterarDadosBtn, 2, 0);

        PainelConteudo.getChildren().addAll(topBar, welcomeLabel, grid);

        BorderPane mainLayout = new BorderPane();
        mainLayout.setCenter(background);
        StackPane.setAlignment(PainelConteudo, Pos.CENTER);
        background.getChildren().add(PainelConteudo);

        Scene scene = new Scene(mainLayout, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void mostrarMenuPaciente(UsuarioPaciente paciente) {

        StackPane background = new StackPane();
        background.setStyle("-fx-background-color: linear-gradient(to top, #3fd0c9,#7FFFD4 ); -fx-background-radius: 30;");
        background.setPadding(new Insets(20));

        VBox contentPanel = new VBox(20);
        contentPanel.setAlignment(Pos.TOP_CENTER); // Align to top left for welcome message
        contentPanel.setPadding(new Insets(20, 10, 20, 10)); // Adjusted top padding to be handled by topBar
        contentPanel.setStyle("-fx-background-color: #f8fefc; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0.5, 0, 0);");
        contentPanel.setMaxWidth(700); // Adjust max width as needed
        contentPanel.setMaxHeight(400); // Adjust max height as needed


        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10));

        topBar.setStyle("-fx-background-color: #66e2d4; " +
                "-fx-background-radius: 20 20 20 20;");

        HBox leftTopBar = new HBox(5);
        leftTopBar.setAlignment(Pos.CENTER_LEFT);
        StackPane logoContainer = new StackPane();
        logoContainer.setPrefSize(30, 30);
        logoContainer.setStyle("-fx-background-color: #fff; -fx-background-radius: 15;");
        Region logoCircle = new Region();
        logoCircle.setPrefSize(20, 20);
        logoCircle.setStyle("-fx-background-color: #66e2d4; -fx-background-radius: 10;");
        logoContainer.getChildren().add(logoCircle);
        Label hygieaLabel = new Label("Hygiea Health");
        hygieaLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        hygieaLabel.setTextFill(Color.WHITE);
        leftTopBar.getChildren().addAll(logoContainer, hygieaLabel);
        HBox.setHgrow(leftTopBar, Priority.ALWAYS);

        HBox rightTopBar = new HBox(5);
        rightTopBar.setAlignment(Pos.CENTER_RIGHT);
        Label nomeUsuarioLabel = new Label(paciente.getNome());
        nomeUsuarioLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        nomeUsuarioLabel.setTextFill(Color.WHITE);
        StackPane userIconContainer = new StackPane();
        userIconContainer.setPrefSize(30, 30);
        userIconContainer.setStyle("-fx-background-color: #fff; -fx-background-radius: 15;");
        Label userIcon = new Label("\uD83D\uDC64"); // Unicode for person icon
        userIcon.setFont(Font.font("Arial", 18));
        userIconContainer.getChildren().add(userIcon);
        Button sairBtn = new Button("Sair");
        sairBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold;");
        sairBtn.setOnAction(e -> mostrarTelaLogin());

        rightTopBar.getChildren().addAll(nomeUsuarioLabel, userIconContainer, sairBtn);

        topBar.getChildren().addAll(leftTopBar, rightTopBar);
        VBox.setMargin(topBar, new Insets(0,0,10,0)); // Add some space below the top bar

        Label welcomeLabel = new Label("Bem-Vindo!");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        welcomeLabel.setPadding(new Insets(20, 0, 20, 0));

        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(30);
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(0, 0, 0, 0));

        Button buscarMedicosBtn = criarBotaoMenu("\uD83D\uDD0D", "Buscar Médicos Disponiveis");
        buscarMedicosBtn.setOnAction(e -> {
             mostrarTelaBuscarMedicos(paciente);
        });

        Button agendarConsultaBtn = criarBotaoMenu("\uD83D\uDCC5", "Agendar Consulta");
        agendarConsultaBtn.setOnAction(e -> {
            mostrarTelaAgendarConsulta(paciente);
        });

        Button cancelarConsultaBtn = criarBotaoMenu("\uD83D\uDDD1️", "Cancelar Consulta");
        cancelarConsultaBtn.setOnAction(e -> {
            mostrarTelaCancelarConsulta(paciente);
        });

        Button avaliarConsultaBtn = criarBotaoMenu("\uD83C\uDF1F\uD83C\uDF1F\uD83C\uDF1F", "Avaliar Consulta");
        avaliarConsultaBtn.setOnAction(e -> {
            mostrarTelaAvaliarConsulta(paciente);
        });

        Button verConsultasBtn = criarBotaoMenu("\uD83D\uDC40", "Ver Consultas");
        verConsultasBtn.setOnAction(e -> {
            mostrarTelaVerConsultas(paciente);
        });

        Button alterarDadosBtn = criarBotaoMenu("\uD83D\uDCBE", "Alterar Dados");
        alterarDadosBtn.setOnAction(e -> {
            mostrarTelaAlterarDados(paciente);
        });

        Button pagarBoletoBtn = criarBotaoMenu("\uD83D\uDCB3", "Pagar Boleto");
        pagarBoletoBtn.setOnAction(
                e -> mostrarTelaPagarBoleto(paciente)
        );

        Button verBoletoBtn = criarBotaoMenu("\uD83C\uDFE6", "Visualizar Boletos");
        verBoletoBtn.setOnAction(
                e -> mostrarTodosBoletosPaciente(paciente)
        );

        grid.add(pagarBoletoBtn, 1, 2);
        grid.add(verBoletoBtn, 0, 2);
        grid.add(buscarMedicosBtn, 0, 0);
        grid.add(agendarConsultaBtn, 1, 0);
        grid.add(cancelarConsultaBtn, 2, 0);
        grid.add(avaliarConsultaBtn, 0, 1);
        grid.add(verConsultasBtn, 1, 1);
        grid.add(alterarDadosBtn, 2, 1);

        contentPanel.getChildren().addAll(topBar, welcomeLabel, grid);

        BorderPane mainLayout = new BorderPane();
        mainLayout.setCenter(background);
        StackPane.setAlignment(contentPanel, Pos.CENTER);
        background.getChildren().add(contentPanel);

        Scene scene = new Scene(mainLayout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Button criarBotaoMenu(String icon, String text) {
        Button button = new Button();
        button.setPrefSize(180, 140);

        button.setStyle("-fx-background-color: linear-gradient(to bottom,#3fd0c9,#6fd1cc ); -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0.5, 0, 0);");
        button.setContentDisplay(ContentDisplay.TOP);
        button.setAlignment(Pos.CENTER);


        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        iconLabel.setTextFill(Color.web("#F0F8FF"));

        Label textLabel = new Label(text);
        textLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        textLabel.setWrapText(true);
        textLabel.setTextAlignment(TextAlignment.CENTER);
        textLabel.setTextFill(Color.web("#F0F8FF"));

        VBox content = new VBox(5, iconLabel, textLabel);
        content.setAlignment(Pos.CENTER);
        button.setGraphic(content);
        return button;
    }

    private StackPane montarLayoutBase(VBox formBox) {

        StackPane background = new StackPane();
        background.setStyle("-fx-background-color: linear-gradient(to top, #3fd0c9, #7FFFD4); -fx-background-radius: 30;");
        background.setPadding(new Insets(20));

        VBox logoBox = new VBox();
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(0, 0, 0, 30));

        try {
            Image logoImage = new Image("file:imagens/logo.png");
            ImageView logoImageView = new ImageView(logoImage);
            logoImageView.setPreserveRatio(true);
            logoImageView.setFitWidth(300);
            logoBox.getChildren().add(logoImageView);
        } catch (Exception e) {
            System.err.println("Erro ao carregar a imagem" + e.getMessage());
        }

        VBox formPanel = new VBox(formBox);
        formPanel.setAlignment(Pos.CENTER);
        formPanel.setPadding(new Insets(30));
        formPanel.setStyle("-fx-background-color: #f8fefc; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0.5, 0, 0);");
        formPanel.setMaxWidth(400);

        BorderPane overlay = new BorderPane();
        overlay.setLeft(logoBox);
        overlay.setCenter(formPanel);
        BorderPane.setMargin(formPanel, new Insets(0, 0, 0, 20));

        background.getChildren().add(overlay);
        return background;
    }


    private void mostrarTelaAlterarDados(Usuario usuario) {
        VBox form = new VBox(15);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(30));

        Label titulo = new Label("Alterar Meus Dados");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        TextField nomeField = new TextField(usuario.getNome());
        nomeField.setPromptText("Nome:");
        nomeField.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");

        TextField senhaField = new TextField(usuario.getSenha());
        senhaField.setPromptText("Nova senha (deixe em branco para manter atual):");
        senhaField.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");

        TextField campoExtra1 = new TextField();
        TextField campoExtra2 = new TextField();

        if (usuario instanceof UsuarioMedico medico) {
            campoExtra1.setText(medico.getEspecialidade());
            campoExtra1.setPromptText("Especialidade:");
            campoExtra2.setText(String.join(",", medico.getPlanosSaudeAtendidos()));
            campoExtra2.setPromptText("Planos de saúde (separados por vírgula):");
        } else if (usuario instanceof UsuarioPaciente paciente) {
            campoExtra1.setText(String.valueOf(paciente.getIdade()));
            campoExtra1.setPromptText("Idade:");
            campoExtra2.setText(paciente.getPlanoSaude());
            campoExtra2.setPromptText("Plano de saúde:");
        }

        campoExtra1.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");
        campoExtra2.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");

        Button salvarButton = new Button("SALVAR");
        salvarButton.setMaxWidth(Double.MAX_VALUE);
        salvarButton.setStyle("-fx-background-color: linear-gradient(to right, #66e2d4, #3fd0c9); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-border-radius: 20;");

        salvarButton.setOnAction(e -> {
            String novoNome = nomeField.getText().trim();
            String novaSenha = senhaField.getText().trim();

            if (!novoNome.isEmpty()) usuario.setNome(novoNome);
            if (!novaSenha.isEmpty()) usuario.setSenha(novaSenha);

            if (usuario instanceof UsuarioMedico medico) {
                medico.setEspecialidade(campoExtra1.getText().trim());
                List<String> planos = Arrays.stream(campoExtra2.getText().split(",")).map(String::trim).toList();
                medico.setPlanosSaudeAtendidos(planos);
            } else if (usuario instanceof UsuarioPaciente paciente) {
                try {
                    paciente.setIdade(Integer.parseInt(campoExtra1.getText().trim()));
                } catch (NumberFormatException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Idade inválida.");
                    alert.show();
                    return;
                }
                paciente.setPlanoSaude(campoExtra2.getText().trim());
            }

            EscreverArquivo.escreverDadosUsuario(usuario);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Dados atualizados com sucesso!");
            alert.showAndWait();
            mostrarTelaPrincipal(usuario);
        });

        Button cancelarButton = new Button("CANCELAR");
        cancelarButton.setOnAction(e -> mostrarTelaPrincipal(usuario));
        cancelarButton.setMaxWidth(Double.MAX_VALUE);
        cancelarButton.setStyle("-fx-background-color: linear-gradient(to right, #007c8a, #68d7e3); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-border-radius: 20;");


        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        grid.add(new Label("Nome:"), 0, 0);
        grid.add(nomeField, 1, 0);

        grid.add(new Label("Senha:"), 0, 1);
        grid.add(senhaField, 1, 1);

        if (usuario instanceof UsuarioMedico) {
            grid.add(new Label("Especialidade:"), 0, 2);
            grid.add(campoExtra1, 1, 2);

            grid.add(new Label("Planos de saúde:"), 0, 3);
            grid.add(campoExtra2, 1, 3);
        } else {
            grid.add(new Label("Idade:"), 0, 2);
            grid.add(campoExtra1, 1, 2);

            grid.add(new Label("Plano de saúde:"), 0, 3);
            grid.add(campoExtra2, 1, 3);
        }

        form.getChildren().addAll(
                titulo,
                grid,
                salvarButton,
                cancelarButton
        );


        StackPane root = montarLayoutBase(form);
        Scene cena = new Scene(root, 800, 500);
        primaryStage.setScene(cena);
        primaryStage.show();
    }

    private void mostrarTelaBuscarMedicos(UsuarioPaciente paciente) {
        Label titulo = new Label("Médicos Disponíveis");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titulo.setTextFill(Color.web("#333"));

        VBox cardContainer = new VBox(15);
        cardContainer.setPadding(new Insets(10));
        cardContainer.setAlignment(Pos.TOP_CENTER);
        cardContainer.setStyle("-fx-background-color: white;");

        File pastaMedicos = new File("dados_usuarios/medicos");
        File[] arquivos = pastaMedicos.listFiles();

        if (arquivos != null) {
            for (File arquivo : arquivos) {
                if (arquivo.getName().endsWith(".txt")) {
                    Map<String, String> dados = LerArquivo.lerCamposDoArquivo(arquivo.getAbsolutePath());
                    if (dados != null) {
                        String planos = dados.get("Planos_de_Saúde_Atendidos");
                        if (planos != null && planos.contains(paciente.getPlanoSaude())) {

                            String nomeMedico = dados.get("Nome");
                            int idMedico = Integer.parseInt(dados.get("ID"));
                            double media = LerArquivo.calcularMediaAvaliacoesMedico(idMedico);

                            String estrelas = gerarEstrelas(media);

                            VBox card = new VBox(5);
                            card.setPadding(new Insets(10));
                            card.setAlignment(Pos.CENTER_LEFT);
                            card.setMaxWidth(600);
                            card.setStyle("""
                            -fx-background-color: #ffffff;
                            -fx-border-color: #66e2d4;
                            -fx-border-radius: 10;
                            -fx-background-radius: 10;
                            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);
                        """);

                            Label nome = new Label("Nome: " + nomeMedico);
                            nome.setFont(Font.font("Arial", FontWeight.BOLD, 16));

                            Label especialidade = new Label("Especialidade: " + dados.get("Especialidade"));
                            Label planosAceitos = new Label("Planos: " + planos);

                            Label avaliacao = new Label("Avaliação: " + estrelas + " (" + String.format("%.1f", media) + ")");
                            avaliacao.setFont(Font.font("Segoe UI Emoji", 16));

                            card.getChildren().addAll(nome, especialidade, planosAceitos, avaliacao);
                            cardContainer.getChildren().add(card);
                        }
                    }
                }
            }
        }
        //barra de rolagem
        ScrollPane scrollPane = new ScrollPane(cardContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: white; -fx-border-color: transparent;");

        Button voltarBtn = new Button("VOLTAR");
        voltarBtn.setStyle("-fx-background-color: #66e2d4; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
        voltarBtn.setOnAction(e -> mostrarTelaPrincipal(paciente));

        VBox layoutFinal = new VBox(20);
        layoutFinal.setAlignment(Pos.TOP_CENTER);
        layoutFinal.setPadding(new Insets(1));
        layoutFinal.setStyle("-fx-background-color: white;");
        layoutFinal.getChildren().addAll(titulo, scrollPane, voltarBtn);

        StackPane root = montarLayoutBase(layoutFinal);
        Scene cena = new Scene(root, 800, 500);
        primaryStage.setScene(cena);
        primaryStage.show();

    }

    String gerarEstrelas(double media) {
        StringBuilder estrelas = new StringBuilder();
        int cheias = (int) media;

        // estrelas amarelas cheias
        for (int i = 0; i < cheias; i++) {
            estrelas.append("⭐");
        }

        // estrelas apagadas para completar 5
        for (int i = cheias; i < 5; i++) {
            estrelas.append("☆");
        }
        return estrelas.toString();
    }




    private void mostrarTelaAgendarConsulta(UsuarioPaciente paciente) {
        AgendadordeConsulta agendador = new AgendadordeConsulta(paciente);

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.TOP_CENTER);

        Label titulo = new Label("Agendar Consulta");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 28));

        HBox medicoBox = criarLinhaInput("Médico:", agendador.getListaNomesMedicos());
        ComboBox<String> medicoComboBox = (ComboBox<String>) medicoBox.getChildren().get(1);

        HBox dataBox = criarLinhaInput("Data:", new ArrayList<>());
        ComboBox<LocalDate> dataComboBox = (ComboBox<LocalDate>) dataBox.getChildren().get(1);

        HBox horarioBox = criarLinhaInput("Horário:", new ArrayList<>());
        ComboBox<LocalTime> horarioComboBox = (ComboBox<LocalTime>) horarioBox.getChildren().get(1);

        medicoComboBox.setOnAction(e -> {
            dataComboBox.getItems().clear();
            horarioComboBox.getItems().clear();
            dataComboBox.getItems().addAll(agendador.obterDatasDisponiveis());
        });

        dataComboBox.setOnAction(e -> {
            String medico = medicoComboBox.getValue();
            LocalDate data = dataComboBox.getValue();
            if (medico != null && data != null) {
                List<LocalTime> horarios = agendador.obterHorariosDisponiveis(medico, data);
                horarioComboBox.getItems().clear();
                horarioComboBox.getItems().addAll(horarios);
            }
        });

        Button confirmarBtn = new Button("Confirmar Agendamento");
        confirmarBtn.setStyle("-fx-background-color: linear-gradient(to right, #66e2d4, #3fd0c9); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20;");
        confirmarBtn.setOnAction(e -> {
            String medico = medicoComboBox.getValue();
            LocalDate data = dataComboBox.getValue();
            LocalTime hora = horarioComboBox.getValue();

            if (medico == null || data == null || hora == null) {
                new Alert(Alert.AlertType.ERROR, "Por favor, selecione médico, data e horário.").showAndWait();
                return;
            }

            List<LocalDate> datas = agendador.obterDatasDisponiveis();
            int indiceData = datas.indexOf(data) + 1;

            List<LocalTime> horarios = agendador.obterHorariosDisponiveis(medico, data);
            int indiceHora = horarios.indexOf(hora) + 1;

            if (indiceData <= 0 || indiceHora <= 0) {
                new Alert(Alert.AlertType.ERROR, "Erro ao processar data ou horário selecionado.").showAndWait();
                return;
            }

            agendador.agendarConsulta(medico, indiceData, indiceHora);
            new Alert(Alert.AlertType.INFORMATION, "Consulta agendada com sucesso!").showAndWait();
            mostrarMenuPaciente(paciente);
        });

        Button voltarBtn = new Button("Voltar");
        voltarBtn.setOnAction(e -> mostrarMenuPaciente(paciente));
        voltarBtn.setStyle("-fx-background-color: #66e2d4; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20;");

        layout.getChildren().addAll(titulo, medicoBox, dataBox, horarioBox, confirmarBtn, voltarBtn);

        StackPane root = montarLayoutBase(layout);
        Scene cena = new Scene(root, 800, 500);
        primaryStage.setScene(cena);
        primaryStage.show();
    }
    private HBox criarLinhaInput(String labelTexto, List<?> opcoes) {
        Label label = new Label(labelTexto);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        label.setMinWidth(100);

        ComboBox comboBox = new ComboBox<>();
        comboBox.getItems().addAll(opcoes);
        comboBox.setPromptText("Selecione");
        comboBox.setPrefWidth(300);

        HBox linha = new HBox(10);
        linha.setAlignment(Pos.CENTER_LEFT);
        linha.getChildren().addAll(label, comboBox);

        return linha;
    }

    private ScrollPane criarCardsConsultas(
            UsuarioPaciente paciente,
            boolean permitirSelecao,
            Consumer<Consulta> aoSelecionarConsulta,
            Consulta[] consultaSelecionadaOut,
            Set<StatusConsulta> statusPermitidos
    ) {
        AgendadordeConsulta agendador = new AgendadordeConsulta(paciente);
        List<Consulta> consultas = agendador.getConsultasFiltradasPorStatus(statusPermitidos);

        VBox cardContainer = new VBox(10);
        cardContainer.setPadding(new Insets(10));
        cardContainer.setAlignment(Pos.TOP_CENTER);

        for (Consulta consulta : consultas) {
            VBox card = new VBox(5);
            card.setPadding(new Insets(10));
            card.setMaxWidth(600);
            card.setStyle("""
            -fx-background-color: #ffffff;
            -fx-border-color: #66e2d4;
            -fx-border-radius: 10;
            -fx-background-radius: 10;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);
        """);

            Label medico = new Label("Médico: " + consulta.getMedico().getNome());
            Label data = new Label("Data: " + consulta.getDataHoraConsulta().toLocalDate());
            Label hora = new Label("Hora: " + consulta.getDataHoraConsulta().toLocalTime());
            Label status = new Label("Status: " + consulta.getStatusConsulta());

            medico.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            data.setFont(Font.font("Arial", 13));
            hora.setFont(Font.font("Arial", 13));
            status.setFont(Font.font("Arial", FontPosture.ITALIC, 12));
            status.setTextFill(Color.web("#555"));

            card.getChildren().addAll(medico, data, hora, status);

            if (permitirSelecao) {
                card.setOnMouseClicked(e -> {
                    for (Node node : cardContainer.getChildren()) {
                        node.setStyle("""
                        -fx-background-color: #ffffff;
                        -fx-border-color: #66e2d4;
                        -fx-border-radius: 10;
                        -fx-background-radius: 10;
                        -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);
                    """);
                    }

                    card.setStyle("""
                    -fx-background-color: #ffffff;
                    -fx-border-color: #0fa3b1;
                    -fx-border-width: 3;
                    -fx-border-radius: 10;
                    -fx-background-radius: 10;
                    -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 6, 0, 0, 2);
                """);

                    consultaSelecionadaOut[0] = consulta;
                    aoSelecionarConsulta.accept(consulta);
                });
            }

            cardContainer.getChildren().add(card);
        }

        ScrollPane scrollPane = new ScrollPane(cardContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: white; -fx-border-color: transparent;");

        return scrollPane;
    }

    private void mostrarTelaCancelarConsulta(UsuarioPaciente paciente) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_CENTER);

        Label titulo = new Label("Cancelar Consulta");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        final Consulta[] consultaSelecionada = new Consulta[1];
        AgendadordeConsulta agendador = new AgendadordeConsulta(paciente);

        ScrollPane scrollPane = criarCardsConsultas(
                paciente,
                true,
                consulta -> consultaSelecionada[0] = consulta,
                consultaSelecionada,
                Set.of(StatusConsulta.AGENDADA)
        );

        Button cancelarBtn = new Button("Cancelar Consulta");
        cancelarBtn.setStyle("-fx-background-color: #66e2d4; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
        cancelarBtn.setOnAction(e -> {
            if (consultaSelecionada[0] == null) {
                new Alert(Alert.AlertType.ERROR, "Selecione uma consulta para cancelar.").showAndWait();
                return;
            }

            Consulta c = consultaSelecionada[0];
            String dataStr = c.getDataHoraConsulta().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String horaStr = c.getDataHoraConsulta().toLocalTime().toString();
            String nomeMedico = c.getMedico().getNome();

            agendador.cancelarConsulta(dataStr, horaStr, nomeMedico);
            new Alert(Alert.AlertType.INFORMATION, "Consulta cancelada com sucesso!").showAndWait();
            mostrarMenuPaciente(paciente);
        });

        Button voltarBtn = new Button("Voltar");
        voltarBtn.setStyle("-fx-background-color: #66e2d4; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
        voltarBtn.setOnAction(e -> mostrarMenuPaciente(paciente));

        content.getChildren().addAll(titulo, scrollPane, cancelarBtn, voltarBtn);
        Scene cena = new Scene(montarLayoutBase(content), 800, 500);
        primaryStage.setScene(cena);
        primaryStage.show();
    }

    private void mostrarTelaAvaliarConsulta(UsuarioPaciente paciente) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_CENTER);

        Label titulo = new Label("Avaliar Consulta");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Avaliacao avaliador = new Avaliacao(paciente);
        final Consulta[] consultaSelecionada = new Consulta[1];
        final int[] avaliacaoSelecionada = {0};

        ScrollPane scrollPane = criarCardsConsultas(
                paciente,
                true,
                consulta -> consultaSelecionada[0] = consulta,
                consultaSelecionada,
                Set.of(StatusConsulta.REALIZADA)
        );

        Label estrelasLabel = new Label("Avaliação (1 a 5 estrelas):");
        estrelasLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        HBox estrelasBox = new HBox(5);
        estrelasBox.setAlignment(Pos.CENTER);
        Label[] estrelas = new Label[5];

        for (int i = 0; i < 5; i++) {
            Label estrela = new Label("☆");
            estrela.setFont(Font.font("Arial", FontWeight.BOLD, 30));
            estrela.setTextFill(Color.GREY);
            final int valor = i + 1;

            estrela.setOnMouseClicked(e -> {
                avaliacaoSelecionada[0] = valor;
                for (int j = 0; j < 5; j++) {
                    estrelas[j].setText(j < valor ? "★" : "☆");
                    estrelas[j].setTextFill(j < valor ? Color.GOLD : Color.GREY);
                }
            });

            estrelas[i] = estrela;
            estrelasBox.getChildren().add(estrela);
        }

        Label comentarioLabel = new Label("Comentário:");
        comentarioLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        TextArea comentario = new TextArea();
        comentario.setPromptText("Escreva sua avaliação...");
        comentario.setPrefRowCount(3);
        comentario.setMaxWidth(500);

        Button avaliarBtn = new Button("Enviar Avaliação");
        avaliarBtn.setStyle("-fx-background-color: #66e2d4; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
        avaliarBtn.setOnAction(e -> {
            if (consultaSelecionada[0] == null) {
                new Alert(Alert.AlertType.ERROR, "Selecione uma consulta para avaliar.").showAndWait();
                return;
            }

            String texto = comentario.getText();
            if (texto.isBlank()) {
                new Alert(Alert.AlertType.ERROR, "Comentário não pode ser vazio.").showAndWait();
                return;
            }

            if (avaliacaoSelecionada[0] == 0) {
                new Alert(Alert.AlertType.ERROR, "Selecione uma quantidade de estrelas.").showAndWait();
                return;
            }

            Consulta c = consultaSelecionada[0];
            String dataStr = c.getDataHoraConsulta().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String horaStr = c.getDataHoraConsulta().toLocalTime().toString();
            String nomeMedico = c.getMedico().getNome();

            avaliador.avaliarConsulta(dataStr, horaStr, nomeMedico, texto, avaliacaoSelecionada[0]);
            new Alert(Alert.AlertType.INFORMATION, "Avaliação enviada com sucesso!").showAndWait();
            mostrarMenuPaciente(paciente);
        });

        Button voltarBtn = new Button("Voltar");
        voltarBtn.setStyle("-fx-background-color: #66e2d4; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
        voltarBtn.setOnAction(e -> mostrarMenuPaciente(paciente));

        content.getChildren().addAll(
                titulo,
                scrollPane,
                estrelasLabel,
                estrelasBox,
                comentarioLabel,
                comentario,
                avaliarBtn,
                voltarBtn
        );

        Scene cena = new Scene(montarLayoutBase(content), 800, 600);
        primaryStage.setScene(cena);
        primaryStage.show();
    }



    private void mostrarTelaVerConsultas(UsuarioPaciente paciente) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_CENTER);

        Label titulo = new Label("Minhas Consultas");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        ScrollPane scrollPane = criarCardsConsultas(
                paciente,
                false,
                consulta -> {},
                new Consulta[1],
                EnumSet.allOf(StatusConsulta.class)
        );


        Button voltarBtn = new Button("Voltar");
        voltarBtn.setStyle("-fx-background-color: #66e2d4; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
        voltarBtn.setOnAction(e -> mostrarMenuPaciente(paciente));

        content.getChildren().addAll(titulo, scrollPane, voltarBtn);
        Scene cena = new Scene(montarLayoutBase(content), 800, 500);
        primaryStage.setScene(cena);
        primaryStage.show();
    }

    private void mostrarTelaVisualizarAgendamentos(UsuarioMedico medico) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_CENTER);

        Label titulo = new Label("Meus Agendamentos");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        VBox cardContainer = new VBox(10);
        cardContainer.setAlignment(Pos.TOP_CENTER);
        cardContainer.setPadding(new Insets(10));

        String caminho = "dados_agendamentos/" + medico.getId() + "/";
        File pasta = new File(caminho);
        File[] arquivos = pasta.listFiles((dir, name) -> name.endsWith(".txt"));

        if (arquivos != null) {
            for (File arq : arquivos) {
                Map<String, String> dados = LerArquivo.lerCamposDoArquivo(arq.getAbsolutePath());
                if (dados != null) {
                    String dataStr = dados.get("Data");
                    String horaStr = dados.get("Hora");
                    String pacienteStr = dados.get("Paciente");
                    String statusStr = dados.getOrDefault("Status", "AGENDADA");

                    VBox card = new VBox(5);
                    card.setPadding(new Insets(10));
                    card.setMaxWidth(600);
                    card.setStyle("""
                    -fx-background-color: #ffffff;
                    -fx-border-color: #66e2d4;
                    -fx-border-radius: 10;
                    -fx-background-radius: 10;
                    -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);
                """);

                    Label pacienteLabel = new Label("Paciente: " + (pacienteStr != null ? pacienteStr : "Desconhecido"));
                    Label dataLabel = new Label("Data: " + dataStr);
                    Label horaLabel = new Label("Hora: " + horaStr);
                    Label statusLabel = new Label("Status: " + statusStr);

                    pacienteLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    dataLabel.setFont(Font.font("Arial", 13));
                    horaLabel.setFont(Font.font("Arial", 13));
                    statusLabel.setFont(Font.font("Arial", FontPosture.ITALIC, 12));
                    statusLabel.setTextFill(Color.web("#555"));

                    card.getChildren().addAll(pacienteLabel, dataLabel, horaLabel, statusLabel);
                    cardContainer.getChildren().add(card);
                }
            }
        }

        ScrollPane scrollPane = new ScrollPane(cardContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: white; -fx-border-color: transparent;");

        Button voltarBtn = new Button("Voltar");
        voltarBtn.setStyle("-fx-background-color: #66e2d4; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
        voltarBtn.setOnAction(e -> mostrarMenuMedico(medico));

        content.getChildren().addAll(titulo, scrollPane, voltarBtn);

        Scene cena = new Scene(montarLayoutBase(content), 800, 500);
        primaryStage.setScene(cena);
        primaryStage.show();
    }
    private void mostrarTelaSelecionarConsultaParaRealizar(UsuarioMedico medico) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_CENTER);

        Label titulo = new Label("Consultas Agendadas");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        VBox cardContainer = new VBox(10);
        cardContainer.setAlignment(Pos.TOP_CENTER);
        cardContainer.setPadding(new Insets(10));

        File pasta = new File("dados_agendamentos/" + medico.getId());
        File[] arquivos = pasta.listFiles((dir, name) -> name.endsWith(".txt"));

        final File[] consultaSelecionada = new File[1];

        if (arquivos != null) {
            for (File arq : arquivos) {
                Map<String, String> dados = LerArquivo.lerCamposDoArquivo(arq.getAbsolutePath());
                if (dados != null && "AGENDADA".equalsIgnoreCase(dados.getOrDefault("Status", "AGENDADA"))) {
                    String dataStr = dados.get("Data");
                    String horaStr = dados.get("Hora");
                    String pacienteStr = dados.get("Paciente");

                    VBox card = new VBox(5);
                    card.setPadding(new Insets(10));
                    card.setMaxWidth(600);
                    card.setStyle("""
                    -fx-background-color: #ffffff;
                    -fx-border-color: #66e2d4;
                    -fx-border-radius: 10;
                    -fx-background-radius: 10;
                    -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);
                """);

                    Label pacienteLabel = new Label("Paciente: " + pacienteStr);
                    Label dataLabel = new Label("Data: " + dataStr);
                    Label horaLabel = new Label("Hora: " + horaStr);

                    pacienteLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    dataLabel.setFont(Font.font("Arial", 13));
                    horaLabel.setFont(Font.font("Arial", 13));

                    card.setOnMouseClicked(event -> {
                        for (Node other : cardContainer.getChildren()) {
                            other.setStyle("""
                            -fx-background-color: #ffffff;
                            -fx-border-color: #66e2d4;
                            -fx-border-radius: 10;
                            -fx-background-radius: 10;
                            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);
                        """);
                        }

                        card.setStyle("""
                        -fx-background-color: #ffffff;
                        -fx-border-color: #0fa3b1;
                        -fx-border-width: 3;
                        -fx-border-radius: 10;
                        -fx-background-radius: 10;
                        -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 6, 0, 0, 2);
                    """);

                        consultaSelecionada[0] = arq;
                    });

                    card.getChildren().addAll(pacienteLabel, dataLabel, horaLabel);
                    cardContainer.getChildren().add(card);
                }
            }
        }

        ScrollPane scrollPane = new ScrollPane(cardContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: white; -fx-border-color: transparent;");

        Button continuarBtn = new Button("Continuar");
        continuarBtn.setStyle("-fx-background-color: #0fa3b1; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
        continuarBtn.setOnAction(e -> {
            if (consultaSelecionada[0] != null) {
                mostrarTelaPreencherConsulta(medico, consultaSelecionada[0]);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Selecione uma consulta para continuar.");
                alert.showAndWait();
            }
        });

        Button voltarBtn = new Button("Voltar");
        voltarBtn.setStyle("-fx-background-color: #66e2d4; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
        voltarBtn.setOnAction(e -> mostrarMenuMedico(medico));

        VBox botoes = new VBox(10, continuarBtn, voltarBtn);
        botoes.setAlignment(Pos.CENTER);

        content.getChildren().addAll(titulo, scrollPane, botoes);
        Scene cena = new Scene(montarLayoutBase(content), 800, 500);
        primaryStage.setScene(cena);
        primaryStage.show();
    }

    private void mostrarTelaPreencherConsulta(UsuarioMedico medico, File arquivoConsulta) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_CENTER);

        Label titulo = new Label("Preencher Consulta");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        TextArea sintomasField = new TextArea();
        sintomasField.setPromptText("Sintomas");
        sintomasField.setPrefRowCount(2);

        TextArea tratamentoField = new TextArea();
        tratamentoField.setPromptText("Tratamento sugerido");
        tratamentoField.setPrefRowCount(2);

        TextArea examesField = new TextArea();
        examesField.setPromptText("Exames");
        examesField.setPrefRowCount(2);

        TextArea medicamentosField = new TextArea();
        medicamentosField.setPromptText("Medicamentos");
        medicamentosField.setPrefRowCount(2);

        Button salvarBtn = new Button("Salvar Consulta");
        salvarBtn.setStyle("-fx-background-color: #0fa3b1; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
        salvarBtn.setOnAction(e -> {
            String sintomas = sintomasField.getText();
            String tratamento = tratamentoField.getText();
            String exames = examesField.getText();
            String medicamentos = medicamentosField.getText();

            if (sintomas.isEmpty() || tratamento.isEmpty() || exames.isEmpty() || medicamentos.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Preencha todos os campos.").showAndWait();
                return;
            }

            Map<String, String> dados = LerArquivo.lerCamposDoArquivo(arquivoConsulta.getAbsolutePath());
            if (dados == null) {
                new Alert(Alert.AlertType.ERROR, "Erro ao carregar a consulta.").showAndWait();
                return;
            }

            String data = dados.get("Data");
            String hora = dados.get("Hora");
            String nomePaciente = dados.get("Paciente");
            String idPaciente = "0";

            Matcher matcher = Pattern.compile("\\(ID:\\s*(\\d+)\\)").matcher(nomePaciente);
            if (matcher.find()) {
                idPaciente = matcher.group(1);
            }

            UsuarioPaciente paciente = (UsuarioPaciente) LerArquivo.buscarUsuarioPorId(Integer.parseInt(idPaciente), "Paciente");

            try {
                String nomeLimpo = nomePaciente.replaceAll("\\(ID:.*?\\)", "").trim();
                nomeLimpo = nomeLimpo.replaceAll("[^a-zA-Z0-9_]", "_");

                String pastaConsultas = "dados_consultas/" + medico.getId() + "/";
                new File(pastaConsultas).mkdirs();
                String nomeArquivo = nomeLimpo + "_" + data.replace("/", "-") + "_" + hora.replace(":", "-") + ".txt";
                File consultaFinal = new File(pastaConsultas + nomeArquivo);

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(consultaFinal))) {
                    writer.write("Paciente: " + nomePaciente + "\n");
                    writer.write("Médico: " + medico.getNome() + "\n");
                    writer.write("Data: " + data + "\n");
                    writer.write("Hora: " + hora + "\n");
                    writer.write("Sintomas: " + sintomas + "\n");
                    writer.write("Tratamento: " + tratamento + "\n");
                    writer.write("Exames: " + exames + "\n");
                    writer.write("Medicamentos: " + medicamentos + "\n");
                }

                // Atualiza status da consulta original
                dados.put("Status", "REALIZADA");
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoConsulta))) {
                    for (Map.Entry<String, String> entry : dados.entrySet()) {
                        writer.write(entry.getKey() + ": " + entry.getValue());
                        writer.newLine();
                    }
                }

                if (paciente != null && !paciente.getPlanoSaude().equalsIgnoreCase("Sem")) {
                    Boleto boleto = gerarBoletoComEspecialidadePreco(medico, paciente);
                    String conteudoBoleto = boleto.gerarConteudoBoleto();

                    String nomeArquivoBoleto = "boleto_" + boleto.getCodigo() + ".txt";
                    String pastaBoletos = "dados_boletos/" + paciente.getId() + "/";
                    new File(pastaBoletos).mkdirs();

                    EscreverArquivo.escreverEmArquivo(nomeArquivoBoleto, pastaBoletos, conteudoBoleto);
                }

                new Alert(Alert.AlertType.INFORMATION, "Consulta realizada com sucesso!").showAndWait();
                mostrarMenuMedico(medico);

            } catch (IOException | NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Erro ao salvar a consulta.").showAndWait();
                ex.printStackTrace();
            }
        });

        Button voltarBtn = new Button("Voltar");
        voltarBtn.setStyle("-fx-background-color: #66e2d4; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
        voltarBtn.setOnAction(e -> mostrarTelaSelecionarConsultaParaRealizar(medico));

        content.getChildren().addAll(
                titulo,
                new Label("Sintomas:"), sintomasField,
                new Label("Tratamento sugerido:"), tratamentoField,
                new Label("Exames:"), examesField,
                new Label("Medicamentos:"), medicamentosField,
                salvarBtn, voltarBtn
        );

        Scene cena = new Scene(montarLayoutBase(content), 800, 600);
        primaryStage.setScene(cena);
        primaryStage.show();
    }

    private void mostrarTodosBoletosPaciente(UsuarioPaciente paciente) {
        GerenciadorBoletos gerenciadorBoletos = new GerenciadorBoletos();
        gerenciadorBoletos.carregarBoletosPaciente(paciente.getId());

        List<Boleto> todosBoletos = gerenciadorBoletos.getTodosBoletosPaciente(paciente.getId());

        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(20));

        Label titulo = new Label("Todos os Boletos");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        VBox containerBoletos = new VBox(10);
        containerBoletos.setPadding(new Insets(10));
        containerBoletos.setAlignment(Pos.TOP_CENTER);

        for (Boleto boleto : todosBoletos) {
            VBox card = new VBox(5);
            card.setPadding(new Insets(10));
            card.setStyle("""
            -fx-background-color: #f8fefc;
            -fx-background-radius: 15;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 2);
        """);
            card.setMaxWidth(700);

            Label codigoLabel = new Label("Código: " + boleto.getCodigo());
            Label valorLabel = new Label("Valor: R$" + String.format("%.2f", boleto.getValor()));
            Label vencimentoLabel = new Label("Vencimento: " + boleto.getDataVencimento());
            Label medicoLabel = new Label("Médico: " + boleto.getNomeMedico());
            Label especialidadeLabel = new Label("Especialidade: " + boleto.getEspecialidade());
            Label statusLabel = new Label("Status: " + (boleto.isPago() ? "PAGO" : "PENDENTE"));

            codigoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            valorLabel.setFont(Font.font("Arial", 13));
            vencimentoLabel.setFont(Font.font("Arial", 13));
            medicoLabel.setFont(Font.font("Arial", 13));
            especialidadeLabel.setFont(Font.font("Arial", 13));
            statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            statusLabel.setTextFill(boleto.isPago() ? Color.GREEN : Color.RED);

            VBox infos = new VBox(2, codigoLabel, valorLabel, vencimentoLabel, medicoLabel, especialidadeLabel, statusLabel);

            card.getChildren().add(infos);
            containerBoletos.getChildren().add(card);
        }

        ScrollPane scrollPane = new ScrollPane(containerBoletos);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(400);
        scrollPane.setStyle("-fx-background-color: transparent;");

        Button voltarBtn = new Button("Voltar");
        voltarBtn.setStyle("""
        -fx-background-color: #66e2d4;
        -fx-text-fill: white;
        -fx-font-weight: bold;
        -fx-background-radius: 10;
        -fx-padding: 8 15 8 15;
    """);
        voltarBtn.setOnAction(e -> mostrarMenuPaciente(paciente));

        content.getChildren().addAll(titulo, scrollPane, voltarBtn);

        Scene cena = new Scene(montarLayoutBase(content), 800, 500);
        primaryStage.setScene(cena);
        primaryStage.show();
    }


    private void mostrarTelaPagarBoleto(UsuarioPaciente paciente) {
        GerenciadorBoletos gerenciadorBoletos = new GerenciadorBoletos();
        gerenciadorBoletos.carregarBoletosPaciente(paciente.getId());

        List<Boleto> pendentes = gerenciadorBoletos.listarBoletosPendentes(paciente.getId());
        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(20));

        Label titulo = new Label("Boletos Pendentes");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        ToggleGroup grupoBoletos = new ToggleGroup();
        VBox containerBoletos = new VBox(10);
        containerBoletos.setPadding(new Insets(10));
        containerBoletos.setAlignment(Pos.TOP_CENTER);

        if (pendentes.isEmpty()) {
            Label aviso = new Label("Você não possui boletos pendentes.");
            aviso.setFont(Font.font("Arial", 16));

            Button voltarBtn = new Button("Voltar");
            voltarBtn.setStyle("""
            -fx-background-color: #66e2d4;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 10;
            -fx-padding: 8 15 8 15;
        """);
            voltarBtn.setOnAction(e -> mostrarMenuPaciente(paciente));

            content.getChildren().addAll(titulo, aviso, voltarBtn);
        } else {
            for (Boleto boleto : pendentes) {
                VBox card = new VBox(5);
                card.setPadding(new Insets(10));
                card.setStyle("""
                -fx-background-color: #f8fefc;
                -fx-background-radius: 15;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 2);
            """);
                card.setMaxWidth(700);

                RadioButton radio = new RadioButton();
                radio.setToggleGroup(grupoBoletos);
                radio.setUserData(boleto);

                Label codigoLabel = new Label("Código: " + boleto.getCodigo());
                Label valorLabel = new Label("Valor: R$" + String.format("%.2f", boleto.getValor()));
                Label vencimentoLabel = new Label("Vencimento: " + boleto.getDataVencimento());
                Label medicoLabel = new Label("Médico: " + boleto.getNomeMedico());
                Label especialidadeLabel = new Label("Especialidade: " + boleto.getEspecialidade());

                codigoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                valorLabel.setFont(Font.font("Arial", 13));
                vencimentoLabel.setFont(Font.font("Arial", 13));
                medicoLabel.setFont(Font.font("Arial", 13));
                especialidadeLabel.setFont(Font.font("Arial", 13));

                VBox infos = new VBox(2, codigoLabel, valorLabel, vencimentoLabel, medicoLabel, especialidadeLabel);

                HBox row = new HBox(10, radio, infos);
                row.setAlignment(Pos.CENTER_LEFT);

                card.getChildren().add(row);
                containerBoletos.getChildren().add(card);
            }

            ScrollPane scrollPane = new ScrollPane(containerBoletos);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefViewportHeight(400);
            scrollPane.setStyle("-fx-background: white; -fx-background-color: white;");

            Button gerarQrBtn = new Button("Gerar QR Code");
            gerarQrBtn.setStyle("""
            -fx-background-color: #0fa3b1;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 10;
            -fx-padding: 8 15 8 15;
        """);
            gerarQrBtn.setOnAction(e -> {
                RadioButton selecionado = (RadioButton) grupoBoletos.getSelectedToggle();
                if (selecionado != null) {
                    Boleto boletoSelecionado = (Boleto) selecionado.getUserData();
                    mostrarTelaQRCode(boletoSelecionado, paciente);
                } else {
                    new Alert(Alert.AlertType.WARNING, "Selecione um boleto para gerar o QR Code.").showAndWait();
                }
            });

            Button voltarBtn = new Button("Voltar");
            voltarBtn.setStyle("""
            -fx-background-color: #66e2d4;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 10;
            -fx-padding: 8 15 8 15;
        """);
            voltarBtn.setOnAction(e -> mostrarMenuPaciente(paciente));

            content.getChildren().addAll(titulo, scrollPane, gerarQrBtn, voltarBtn);
        }

        Scene cena = new Scene(montarLayoutBase(content), 800, 500);
        primaryStage.setScene(cena);
        primaryStage.show();
    }



    private void mostrarTelaQRCode(Boleto boleto, UsuarioPaciente paciente) {
        GerenciadorBoletos gerenciadorBoletos = new GerenciadorBoletos();
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        Label titulo = new Label("Pagamento via QR Code");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Image qrImage = gerarQRCode(boleto.getCodigo(), 250, 250);
        ImageView qrView = new ImageView(qrImage);

        Label status = new Label();
        status.setFont(Font.font("Arial", 14));

        Button confirmarPagamento = new Button("Confirmar Pagamento");
        confirmarPagamento.setStyle("""
        -fx-background-color: #0fa3b1;
        -fx-text-fill: white;
        -fx-font-weight: bold;
        -fx-background-radius: 10;
        -fx-padding: 8 15 8 15;
    """);
        confirmarPagamento.setOnAction(e -> {
            gerenciadorBoletos.marcarComoPago(boleto.getCodigo());
            GerenciadorBoletos.atualizarStatusBoleto(paciente, boleto.getCodigo());
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Boleto Pago Com Sucesso ! ");
            alert.showAndWait();
            mostrarMenuPaciente(paciente);
        });


        Button voltarBtn = new Button("Voltar");
        voltarBtn.setStyle("""
        -fx-background-color: #66e2d4;
        -fx-text-fill: white;
        -fx-font-weight: bold;
        -fx-background-radius: 10;
        -fx-padding: 8 15 8 15;
    """);
        voltarBtn.setOnAction(e -> mostrarTelaPagarBoleto(paciente));

        layout.getChildren().addAll(titulo, qrView, confirmarPagamento, status, voltarBtn);

        Scene cena = new Scene(montarLayoutBase(layout), 800, 600);
        primaryStage.setScene(cena);
        primaryStage.show();
    }

    public Image bitMatrixToImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        WritableImage image = new WritableImage(width, height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.getPixelWriter().setColor(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return image;
    }
    private Image gerarQRCode(String texto, int largura, int altura) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    texto, BarcodeFormat.QR_CODE, largura, altura);
            return bitMatrixToImage(bitMatrix);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

}
