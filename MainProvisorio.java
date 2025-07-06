import java.util.ArrayList;
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
                        if (usuarioLogado instanceof UsuarioMedico) {
                            menuMedico((UsuarioMedico) usuarioLogado);
                        } else if (usuarioLogado instanceof UsuarioPaciente) {
                            menuPaciente((UsuarioPaciente) usuarioLogado);
                        }
                        // Após sair do menu específico, limpa o usuário logado
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
            usuarioLogado = usuario; // Salva o usuário logado na memória
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
            case 1:
                System.out.println("\n--- Informações para o Novo Médico ---");
                String especialidade = InputGerenciador.lerString("Digite a especialidade do médico: ");
                List<String> planosSaude = InputGerenciador.lerLista(
                        "Digite os planos de saúde que o médico atende (separados por vírgula, ex: Unimed,Pix): ",
                        ","
                );
                novoUsuario = UsuarioFactory.criarMedico(senha, nome, especialidade, planosSaude);
                break;
            case 2:
                System.out.println("\n--- Informações para o Novo Paciente ---");
                int idade = InputGerenciador.lerInteiro("Digite a idade do paciente: ");
                String planoSaudePaciente = InputGerenciador.lerString("Digite o plano de saúde do paciente ou 'Sem' caso nao tenha: ");
                novoUsuario = UsuarioFactory.criarPaciente(senha, nome, idade, planoSaudePaciente);
                break;
            default:
                System.out.println("Tipo de usuário inválido. A criação do usuário foi cancelada.");
                break;
        }

        if (novoUsuario != null) {
            System.out.println("\n--- Usuário Criado ---");
            System.out.println("ID: " + novoUsuario.getId());
            System.out.println("Nome: " + novoUsuario.getNome());
            System.out.println("Tipo: " + novoUsuario.getTipoUsuario());

            if (novoUsuario instanceof UsuarioMedico) {
                UsuarioMedico medico = (UsuarioMedico) novoUsuario;
                System.out.println("Especialidade: " + medico.getEspecialidade());
                System.out.println("Planos: " + medico.getPlanosSaudeAtendidos());
            } else if (novoUsuario instanceof UsuarioPaciente) {
                UsuarioPaciente paciente = (UsuarioPaciente) novoUsuario;
                System.out.println("Idade: " + paciente.getIdade());
                System.out.println("Plano de Saúde: " + paciente.getPlanoSaude());
            }
            EscreverArquivo.escreverDadosUsuario(novoUsuario);
            System.out.println("Usuário salvo com sucesso!");
        }
    }

    // --- Menus Pós-Login ---

    private static void menuMedico(UsuarioMedico medico) {
        System.out.println("\n=== Menu do Médico: " + medico.getNome() + " (ID: " + medico.getId() + ") ===");
        int opcao;
        do {
            System.out.println("1. Alterar Meus Dados"); // Já implementado
            System.out.println("2. Visualizar Meus Agendamentos"); // Para Implementar
            System.out.println("3. Realizar Consulta"); // Para Implementar
            System.out.println("0. Fazer Logout");
            opcao = InputGerenciador.lerInteiro("Escolha uma opção: ");

            switch (opcao) {
                case 1:
                    alterarDadosUsuarioLogado(medico);
                    break;
                case 2:
                    System.out.println("Funcionalidade de visualizar agendamentos em desenvolvimento.");
                    break;
                case 3:
                    //Implementar: Realizar Consulta
                    // Médico informa descrição (sintomas, tratamento, medicamentos, exames...).
                    // Lógica para gerar conta se paciente não tiver plano.
                    System.out.println("Funcionalidade de realizar consulta em desenvolvimento.");
                    break;
                case 0:
                    System.out.println("Fazendo logout do Dr. " + medico.getNome() + ".");
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        } while (opcao != 0);
    }

    private static void menuPaciente(UsuarioPaciente paciente) {
        System.out.println("\n=== Menu do Paciente: " + paciente.getNome() + " (ID: " + paciente.getId() + ") ===");
        int opcao;
        do {
            System.out.println("1. Alterar Meus Dados"); // Já implementado
            System.out.println("2. Buscar e Visualizar Médicos"); // Para Implementar
            System.out.println("3. Agendar Consulta"); // Para Implementar
            System.out.println("4. Cancelar Agendamento"); // Para Implementar
            System.out.println("5. Avaliar Consulta"); // Para Implementar
            System.out.println("0. Fazer Logout");
            opcao = InputGerenciador.lerInteiro("Escolha uma opção: ");

            switch (opcao) {
                case 1:
                    alterarDadosUsuarioLogado(paciente);
                    break;
                case 2:
                    //Implementar: Buscar e Visualizar Médicos
                    // O paciente deve poder pesquisar médicos por nome/especialidade.
                    // O sistema deve mostrar nome, especialidade, estrelas e últimas avaliações.
                    // Regra: Mostrar apenas médicos que atendem ao plano do paciente; se sem plano, mostra todos.
                    System.out.println("Funcionalidade de buscar e visualizar médicos em desenvolvimento.");
                    break;
                case 3:
                    // Implementar: Agendar Consulta
                    // Selecionar médico e data.
                    // Regra: Médico atende até 3 por dia; se lotado, vai para lista de espera.
                    // Isso envolve a criação de objetos de Agendamento e gerenciamento de horários.
                    System.out.println("Funcionalidade de agendar consulta em desenvolvimento.");
                    break;
                case 4:
                    // Implementar: Cancelar Agendamento
                    // Regra: Se houver lista de espera, aloca o próximo da lista.
                    System.out.println("Funcionalidade de cancelar agendamento em desenvolvimento.");
                    break;
                case 5:
                    // Implementar: Avaliar Consulta
                    // Após uma consulta, o paciente pode dar estrelas (1-5) e um texto.
                    // A avaliação deve ser associada a uma consulta já realizada.
                    System.out.println("Funcionalidade de avaliar consulta em desenvolvimento.");
                    break;
                case 0:
                    System.out.println("Fazendo logout do paciente " + paciente.getNome() + ".");
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        } while (opcao != 0);
    }

    private static void alterarDadosUsuarioLogado(Usuario usuarioParaAlterar) {
        System.out.println("\n--- Alterar Meus Dados ---");

        System.out.println("ID: " + usuarioParaAlterar.getId());
        System.out.println("Nome atual: " + usuarioParaAlterar.getNome());
        System.out.println("Senha atual: " + usuarioParaAlterar.getSenha());

        String novoNome = InputGerenciador.lerString("Novo nome (deixe em branco para manter '" + usuarioParaAlterar.getNome() + "'): ");
        if (!novoNome.isEmpty()) {
            usuarioParaAlterar.setNome(novoNome);
        }
        String novaSenha = InputGerenciador.lerString("Nova senha (deixe em branco para manter a atual): ");
        if (!novaSenha.isEmpty()) {
            usuarioParaAlterar.setSenha(novaSenha);
        }

        if (usuarioParaAlterar instanceof UsuarioMedico) {
            UsuarioMedico medico = (UsuarioMedico) usuarioParaAlterar;
            System.out.println("Especialidade atual: " + medico.getEspecialidade());
            String novaEspecialidade = InputGerenciador.lerString("Nova especialidade (deixe em branco para manter '" + medico.getEspecialidade() + "'): ");
            if (!novaEspecialidade.isEmpty()) {
                medico.setEspecialidade(novaEspecialidade);
            }
            String planosAtuais = String.join(",", medico.getPlanosSaudeAtendidos());
            String novosPlanosStr = InputGerenciador.lerString("Novos planos de saúde (separados por vírgula, deixe em branco para manter '" + planosAtuais + "'): ");
            if (!novosPlanosStr.isEmpty()) {
                List<String> novosPlanos = Arrays.asList(novosPlanosStr.split(",")).stream().map(String::trim).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
                medico.setPlanosSaudeAtendidos(novosPlanos);
            }
            EscreverArquivo.escreverDadosUsuario(medico); // Salva o objeto médico atualizado
            System.out.println("Dados do médico atualizados e salvos com sucesso!");

        } else if (usuarioParaAlterar instanceof UsuarioPaciente) {
            UsuarioPaciente paciente = (UsuarioPaciente) usuarioParaAlterar;
            System.out.println("Idade atual: " + paciente.getIdade());
            System.out.println("Plano de Saúde atual: " + paciente.getPlanoSaude());

            String novaIdadeStr = InputGerenciador.lerString("Nova idade (deixe em branco para manter '" + paciente.getIdade() + "'): ");
            if (!novaIdadeStr.isEmpty()) {
                try {
                    paciente.setIdade(Integer.parseInt(novaIdadeStr));
                } catch (NumberFormatException e) {
                    System.out.println("Idade inválida. Idade não foi alterada.");
                }
            }
            String novoPlanoSaude = InputGerenciador.lerString("Novo plano de saúde (deixe em branco para manter '" + paciente.getPlanoSaude() + "'): ");
            if (!novoPlanoSaude.isEmpty()) {
                paciente.setPlanoSaude(novoPlanoSaude);
            }
            EscreverArquivo.escreverDadosUsuario(paciente);
            System.out.println("Dados do paciente atualizados e salvos com sucesso!");
        }
    }
}