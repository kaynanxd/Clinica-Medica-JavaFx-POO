import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public class MainProvisorio {

    private static Usuario usuarioLogado = null;

    public static void main(String[] args) {
        System.out.println("--- Bem-vindo ao Sistema de Consultas Médicas ---");

        int opcaoPrincipal;
        do {
            System.out.println("\nMenu Principal:");
            System.out.println("1. Fazer Login");
            System.out.println("2. Criar Nova Conta (Usuário)");
            System.out.println("0. Sair");
            opcaoPrincipal = InputGerenciador.lerInteiro("Escolha uma opção: ");

            switch (opcaoPrincipal) {
                case 1:
                    realizarLogin();
                    if (usuarioLogado != null) {
                        if (usuarioLogado instanceof UsuarioMedico medico) {
                            menuMedico(medico);
                        } else if (usuarioLogado instanceof UsuarioPaciente paciente) {
                            menuPaciente(paciente);
                        }
                        usuarioLogado = null;
                    }
                    break;
                case 2:
                    criarNovoUsuario();
                    break;
                case 0:
                    System.out.println("Saindo do sistema. Até mais!");
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        } while (opcaoPrincipal != 0);

        InputGerenciador.closeScanner();
    }

    private static void realizarLogin() {
        System.out.println("\n--- Realizar Login ---");
        String identificador = InputGerenciador.lerString("Digite seu ID ou Nome: ");
        String senha = InputGerenciador.lerString("Digite sua senha: ");

        GerenciadorLogin gerenciadorLogin = new GerenciadorLogin();
        Usuario usuario = gerenciadorLogin.verificarCredenciais(identificador, senha);

        if (usuario != null) {
            System.out.println("\n>> Login realizado com sucesso! <<");
            System.out.println("Bem-vindo(a), " + usuario.getNome() + "!");
            usuarioLogado = usuario;
        } else {
            System.out.println("\n>> Credenciais inválidas! <<");
        }
    }

    private static void criarNovoUsuario() {
        Usuario novoUsuario = null;
        int tipoUsuarioNum = InputGerenciador.lerInteiro("Deseja criar um usuário [1] Médico ou [2] Paciente? ");
        String nome = InputGerenciador.lerString("Digite o nome do usuário: ");
        String senha = InputGerenciador.lerString("Digite a senha do usuário: ");

        switch (tipoUsuarioNum) {
            case 1 -> {
                String especialidade = InputGerenciador.lerString("Digite a especialidade do médico: ");
                List<String> planos = InputGerenciador.lerLista(
                        "Digite os planos de saúde atendidos (ex: Unimed,Amil): ", ",");
                novoUsuario = UsuarioFactory.criarMedico(senha, nome, especialidade, planos);
            }
            case 2 -> {
                int idade = InputGerenciador.lerInteiro("Digite a idade do paciente: ");
                String plano = InputGerenciador.lerString("Digite o plano de saúde ou 'Sem': ");
                novoUsuario = UsuarioFactory.criarPaciente(senha, nome, idade, plano);
            }
            default -> {
                System.out.println("Tipo de usuário inválido.");
                return;
            }
        }

        if (novoUsuario != null) {
            EscreverArquivo.escreverDadosUsuario(novoUsuario);
            System.out.println("Usuário criado com sucesso!");
        }
    }

    private static void menuMedico(UsuarioMedico medico) {
        RealizarConsulta realizador = new RealizarConsulta(medico);
        int opcao;
        do {
            System.out.println("\n=== Menu do Médico " + medico.getNome() + " ===");
            System.out.println("1. Alterar Meus Dados");
            System.out.println("2. Visualizar Meus Agendamentos");
            System.out.println("3. Realizar Consulta");
            System.out.println("0. Logout");
            opcao = InputGerenciador.lerInteiro("Escolha uma opção: ");

            switch (opcao) {
                case 1 -> alterarDadosUsuarioLogado(medico);
                case 2 -> realizador.visualizarConsultas();
                case 3 -> {
                    realizador.visualizarConsultas();
                    String data = InputGerenciador.lerString("Data (dd/MM/yyyy): ");
                    String hora = InputGerenciador.lerString("Horário (HH:mm): ");
                    String idPaciente = InputGerenciador.lerString("ID do paciente: ");
                    String sintomas = InputGerenciador.lerString("Sintomas: ");
                    String tratamento = InputGerenciador.lerString("Tratamento sugerido: ");
                    String exames = InputGerenciador.lerString("Exames: ");
                    String medicamentos = InputGerenciador.lerString("Medicamentos: ");
                    realizador.realizarConsulta(data, hora, idPaciente, sintomas, tratamento, exames, medicamentos);
                }
                case 0 -> System.out.println("Logout realizado.");
                default -> System.out.println("Opção inválida.");
            }
        } while (opcao != 0);
    }

    private static void menuPaciente(UsuarioPaciente paciente) {
        AgendadordeConsulta agendador = new AgendadordeConsulta(paciente);
        Avaliacao avaliador = new Avaliacao(paciente);
        int opcao;
        do {
            System.out.println("\n=== Menu do Paciente " + paciente.getNome() + " ===");
            System.out.println("1. Alterar Meus Dados");
            System.out.println("2. Buscar Médicos");
            System.out.println("3. Agendar Consulta");
            System.out.println("4. Cancelar Consulta");
            System.out.println("5. Avaliar Consulta");
            System.out.println("0. Logout");
            opcao = InputGerenciador.lerInteiro("Escolha uma opção: ");

            switch (opcao) {
                case 1 -> alterarDadosUsuarioLogado(paciente);
                case 2 -> agendador.exibirMedicos();
                case 3 -> {
                    agendador.exibirMedicos();
                    String nomeMedico = InputGerenciador.lerString("Nome do médico: ");

                    List<LocalDate> datasDisponiveis = agendador.obterDatasDisponiveis();
                    System.out.println("Datas disponíveis:");
                    for (int i = 0; i < datasDisponiveis.size(); i++) {
                        System.out.println((i + 1) + " - " + datasDisponiveis.get(i));
                    }

                    int opcaoData = InputGerenciador.lerInteiro("Escolha a data (1 a " + datasDisponiveis.size() + "): ");
                    if (opcaoData < 1 || opcaoData > datasDisponiveis.size()) {
                        System.out.println("Opção de data inválida.");
                        return;
                    }
                    LocalDate dataEscolhida = datasDisponiveis.get(opcaoData - 1);

                    List<LocalTime> horariosDisponiveis = agendador.obterHorariosDisponiveis(nomeMedico, dataEscolhida);
                    if (horariosDisponiveis.isEmpty()) {
                        System.out.println("Todos os horários estão ocupados. Você será adicionado à lista de espera.");
                        agendador.agendarConsulta(nomeMedico, opcaoData, 0); // 0 como código especial para lista de espera
                        return;
                    }

                    System.out.println("Horários disponíveis:");
                    for (int i = 0; i < horariosDisponiveis.size(); i++) {
                        System.out.println((i + 1) + " - " + horariosDisponiveis.get(i));
                    }

                    int opcaoHora = InputGerenciador.lerInteiro("Escolha o horário (1 a " + horariosDisponiveis.size() + "): ");
                    if (opcaoHora < 1 || opcaoHora > horariosDisponiveis.size()) {
                        System.out.println("Opção de horário inválida.");
                        return;
                    }

                    agendador.agendarConsulta(nomeMedico, opcaoData, opcaoHora);
                }
                case 4 -> {
                    System.out.println("Consultas agendadas:");
                    avaliador.visualizarConsultas();
                    String data = InputGerenciador.lerString("Data (dd/MM/yyyy): ");
                    String hora = InputGerenciador.lerString("Horário (HH:mm): ");
                    String nomeMedico = InputGerenciador.lerString("Nome do médico: ");
                    agendador.cancelarConsulta(data, hora, nomeMedico);
                }
                case 5 -> {
                    System.out.println("Consultas agendadas:");
                    avaliador.visualizarConsultas();
                    String data = InputGerenciador.lerString("Data (dd/MM/yyyy): ");
                    String hora = InputGerenciador.lerString("Horário (HH:mm): ");
                    String nomeMedico = InputGerenciador.lerString("Nome do médico: ");
                    String texto = InputGerenciador.lerString("Escreva sua avaliação: ");
                    double estrelas = InputGerenciador.lerInteiro("Quantas estrelas (1 a 5): ");
                    avaliador.avaliarConsulta(data, hora, nomeMedico, texto, estrelas);
                }
                case 0 -> System.out.println("Logout realizado.");
                default -> System.out.println("Opção inválida.");
            }
        } while (opcao != 0);
    }

    private static void alterarDadosUsuarioLogado(Usuario usuario) {
        String novoNome = InputGerenciador.lerString("Novo nome (Enter para manter '" + usuario.getNome() + "'): ");
        if (!novoNome.isEmpty()) usuario.setNome(novoNome);

        String novaSenha = InputGerenciador.lerString("Nova senha (Enter para manter a atual): ");
        if (!novaSenha.isEmpty()) usuario.setSenha(novaSenha);

        if (usuario instanceof UsuarioMedico medico) {
            String novaEsp = InputGerenciador.lerString("Nova especialidade (Enter p/ manter '" + medico.getEspecialidade() + "'): ");
            if (!novaEsp.isEmpty()) medico.setEspecialidade(novaEsp);

            String novosPlanos = InputGerenciador.lerString("Novos planos (separados por vírgula, Enter para manter): ");
            if (!novosPlanos.isEmpty()) {
                List<String> planos = Arrays.asList(novosPlanos.split(",")).stream().map(String::trim).toList();
                medico.setPlanosSaudeAtendidos(planos);
            }

            EscreverArquivo.escreverDadosUsuario(medico);
            System.out.println("Dados do médico atualizados!");
        }

        if (usuario instanceof UsuarioPaciente paciente) {
            String novaIdadeStr = InputGerenciador.lerString("Nova idade (Enter para manter): ");
            if (!novaIdadeStr.isEmpty()) {
                try {
                    paciente.setIdade(Integer.parseInt(novaIdadeStr));
                } catch (NumberFormatException e) {
                    System.out.println("Idade inválida.");
                }
            }

            String novoPlano = InputGerenciador.lerString("Novo plano de saúde (Enter para manter): ");
            if (!novoPlano.isEmpty()) paciente.setPlanoSaude(novoPlano);

            EscreverArquivo.escreverDadosUsuario(paciente);
            System.out.println("Dados do paciente atualizados!");
        }
    }
}
