
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainProvisorio {

    public static void main(String[] args) {
        System.out.println("--- Bem-vindo ao Sistema de Consultas Médicas ---");

        int opcao;
        do {
            System.out.println("\nMenu Principal:");
            System.out.println("1. Criar Novo Usuário");
            System.out.println("2. Alterar Dados de Usuário Existente");
            System.out.println("0. Sair");
            opcao = InputGerenciador.lerInteiro("Escolha uma opção: ");

            switch (opcao) {
                case 1:
                    criarNovoUsuario();
                    break;
                case 2:
                    alterarDadosUsuario();
                    break;
                case 0:
                    System.out.println("Saindo do sistema. Até mais!");
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        } while (opcao != 0);

        InputGerenciador.closeScanner();
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
                String planoSaudePaciente = InputGerenciador.lerString("Digite o plano de saúde do paciente (ou 'Sem' caso nao tenha): ");
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

    private static void alterarDadosUsuario() {
        System.out.println("\n--- Alterar Dados de Usuário Existente ---");
        int tipoUsuarioNum = InputGerenciador.lerInteiro("Deseja alterar dados de [1] Médico ou [2] Paciente? ");
        String tipoUsuarioStr = "";

        switch (tipoUsuarioNum) {
            case 1:
                tipoUsuarioStr = "Medico";
                break;
            case 2:
                tipoUsuarioStr = "Paciente";
                break;
            default:
                System.out.println("Tipo de usuário inválido.");
                return;
        }

        int idBusca = InputGerenciador.lerInteiro("Digite o ID do " + tipoUsuarioStr + " que deseja alterar: ");

        Usuario usuarioExistente = LerArquivo.buscarUsuarioPorId(idBusca, tipoUsuarioStr);

        if (usuarioExistente == null) {
            System.out.println("Usuário " + tipoUsuarioStr + " com ID " + idBusca + " não encontrado.");
            return;
        }

        System.out.println("\n--- Dados Atuais do Usuário ---");
        System.out.println("ID: " + usuarioExistente.getId());
        System.out.println("Nome: " + usuarioExistente.getNome());
        System.out.println("Senha: " + usuarioExistente.getSenha());

        if (usuarioExistente instanceof UsuarioMedico) {
            UsuarioMedico medico = (UsuarioMedico) usuarioExistente;
            System.out.println("Especialidade: " + medico.getEspecialidade());
            System.out.println("Planos: " + medico.getPlanosSaudeAtendidos());

            System.out.println("\n--- Digite os Novos Dados para o Médico (deixe em branco para manter o atual) ---");
            String novoNome = InputGerenciador.lerString("Novo nome (" + medico.getNome() + "): ");
            if (!novoNome.isEmpty()) {medico.setNome(novoNome);}
            String novaSenha = InputGerenciador.lerString("Nova senha (" + medico.getSenha() + "): ");
            if (!novaSenha.isEmpty()) {medico.setSenha(novaSenha);}
            String novaEspecialidade = InputGerenciador.lerString("Nova especialidade (" + medico.getEspecialidade() + "): ");
            if (!novaEspecialidade.isEmpty()) {medico.setEspecialidade(novaEspecialidade);}
            String novosPlanosStr = InputGerenciador.lerString("Novos planos de saúde (separados por vírgula, ex: Unimed,Amil) (" + String.join(",", medico.getPlanosSaudeAtendidos()) + "): ");
            if (!novosPlanosStr.isEmpty()) {
                List<String> novosPlanos = Arrays.asList(novosPlanosStr.split(",")).stream().map(String::trim).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
                medico.setPlanosSaudeAtendidos(novosPlanos);
            }

            // Salva o usuário com os dados alterados (sobrescrevendo o arquivo existente)
            EscreverArquivo.escreverDadosUsuario(medico);
            System.out.println("Dados do médico atualizados e salvos com sucesso!");

        } else if (usuarioExistente instanceof UsuarioPaciente) {
            UsuarioPaciente paciente = (UsuarioPaciente) usuarioExistente;
            System.out.println("Idade: " + paciente.getIdade());
            System.out.println("Plano de Saúde: " + paciente.getPlanoSaude());

            System.out.println("\n--- Digite os Novos Dados para o Paciente (deixe em branco para manter o atual) ---");
            String novoNome = InputGerenciador.lerString("Novo nome (" + paciente.getNome() + "): ");
            if (!novoNome.isEmpty()) {paciente.setNome(novoNome);}
            String novaSenha = InputGerenciador.lerString("Nova senha (" + paciente.getSenha() + "): ");
            if (!novaSenha.isEmpty()) {paciente.setSenha(novaSenha);}
            String novaIdadeStr = InputGerenciador.lerString("Nova idade (" + paciente.getIdade() + "): ");
            if (!novaIdadeStr.isEmpty()) {
                try {
                    paciente.setIdade(Integer.parseInt(novaIdadeStr));
                } catch (NumberFormatException e) {
                    System.out.println("Idade inválida. Idade não foi alterada.");
                }
            }
            String novoPlanoSaude = InputGerenciador.lerString("Novo plano de saúde (" + paciente.getPlanoSaude() + "): ");
            if (!novoPlanoSaude.isEmpty()) {paciente.setPlanoSaude(novoPlanoSaude);}

            // Salva o usuário com os dados alterados (sobrescrevendo o arquivo existente)
            EscreverArquivo.escreverDadosUsuario(paciente);
            System.out.println("Dados do paciente atualizados e salvos com sucesso!");
        }
    }
}