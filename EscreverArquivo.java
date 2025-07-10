
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EscreverArquivo {

    private static final String BASE_PASTA = "dados_usuarios";
    private static final String MEDICOS_PASTA = BASE_PASTA + File.separator + "medicos";
    private static final String PACIENTES_PASTA = BASE_PASTA + File.separator + "pacientes";

    private EscreverArquivo() {
    }

    private static void criarDiretorios() {
        new File(MEDICOS_PASTA).mkdirs();
        new File(PACIENTES_PASTA).mkdirs();
    }

    public static void escreverEmArquivo(String nomeArquivo, String diretorio, String conteudo) {
        new File(diretorio).mkdirs();
        if (!nomeArquivo.endsWith(".txt")) {
            nomeArquivo += ".txt";
        }

        String caminhoCompleto = diretorio + File.separator + nomeArquivo;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(caminhoCompleto))) {
            writer.write(conteudo);
            System.out.println("Arquivo salvo com sucesso em: " + caminhoCompleto);
        } catch (IOException e) {
            System.err.println("Erro ao escrever no arquivo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void escreverDadosUsuario(Usuario usuario) {
        criarDiretorios();

        String diretorioDestino;
        StringBuilder conteudo = new StringBuilder();

        conteudo.append("ID: ").append(usuario.getId()).append("\n");
        conteudo.append("Nome: ").append(usuario.getNome()).append("\n");
        conteudo.append("Tipo: ").append(usuario.getTipoUsuario()).append("\n");
        conteudo.append("Senha: ").append(usuario.getSenha()).append("\n");

        if (usuario instanceof UsuarioMedico medico) {
            diretorioDestino = MEDICOS_PASTA;
            conteudo.append("Especialidade: ").append(medico.getEspecialidade()).append("\n");

            List<String> planos = medico.getPlanosSaudeAtendidos();
            conteudo.append("Planos_de_Saúde_Atendidos: ");
            conteudo.append((planos != null && !planos.isEmpty()) ? String.join(", ", planos) : "Nenhum");
            conteudo.append("\n");

        } else if (usuario instanceof UsuarioPaciente paciente) {
            diretorioDestino = PACIENTES_PASTA;
            conteudo.append("Idade: ").append(paciente.getIdade()).append("\n");
            conteudo.append("Plano_de_Saúde: ").append(paciente.getPlanoSaude()).append("\n");

        } else {
            System.err.println("Erro: Tipo de usuário desconhecido.");
            return;
        }
        escreverEmArquivo(String.valueOf(usuario.getId()), diretorioDestino, conteudo.toString());
    }

    public static void escreverAgendamento(UsuarioMedico medico, UsuarioPaciente paciente, LocalDate data, LocalTime hora) {
        String diretorio = "dados_agendamentos" + File.separator + medico.getId();

        String nomeArquivo = paciente.getId() + "_" + data + "_" + hora.toString().replace(":", "-");

        StringBuilder conteudo = new StringBuilder();
        conteudo.append("Médico: ").append(medico.getNome()).append(" (ID: ").append(medico.getId()).append(")\n");
        conteudo.append("Paciente: ").append(paciente.getNome()).append(" (ID: ").append(paciente.getId()).append(")\n");
        conteudo.append("Data: ").append(data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        conteudo.append("Hora: ").append(hora.format(DateTimeFormatter.ofPattern("HH:mm"))).append("\n");

        escreverEmArquivo(nomeArquivo, diretorio, conteudo.toString());
    }

}