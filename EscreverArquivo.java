
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    public static void escreverDadosUsuario(Usuario usuario) {
        criarDiretorios();

        String diretorioDestino = "";
        StringBuilder conteudo = new StringBuilder();

        conteudo.append("ID: ").append(usuario.getId()).append("\n");
        conteudo.append("Nome: ").append(usuario.getNome()).append("\n");
        conteudo.append("Tipo: ").append(usuario.getTipoUsuario()).append("\n");
        conteudo.append("Senha: ").append(usuario.getSenha()).append("\n");

        if (usuario instanceof UsuarioMedico) {
            UsuarioMedico medico = (UsuarioMedico) usuario;
            diretorioDestino = MEDICOS_PASTA;
            conteudo.append("Especialidade: ").append(medico.getEspecialidade()).append("\n");
            conteudo.append("Planos_de_Saúde_Atendidos: ");
            List<String> planos = medico.getPlanosSaudeAtendidos();
            if (planos != null && !planos.isEmpty()) {
                conteudo.append(String.join(", ", planos));
            } else {
                conteudo.append("Nenhum");
            }
            conteudo.append("\n");

        } else if (usuario instanceof UsuarioPaciente) {
            UsuarioPaciente paciente = (UsuarioPaciente) usuario;
            diretorioDestino = PACIENTES_PASTA;
            conteudo.append("Idade: ").append(paciente.getIdade()).append("\n");
            conteudo.append("Plano_de_Saúde: ").append(paciente.getPlanoSaude()).append("\n");
        } else {
            System.err.println("Erro: Tipo de usuário desconhecido ou não suportado para escrita de arquivo.");
            return;
        }

        // O nome do arquivo será o ID do usuário. Convertemos o int ID para String.
        String caminhoArquivo = diretorioDestino + File.separator + usuario.getId() + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(caminhoArquivo))) {
            writer.write(conteudo.toString());
            System.out.println("Dados do " + usuario.getTipoUsuario() + " '" + usuario.getNome() + "' salvos em: " + caminhoArquivo);
        } catch (IOException e) {
            System.err.println("Erro ao escrever dados do usuário " + usuario.getNome() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}