
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LerArquivo {

    private static final String BASE_PASTA = "dados_usuarios";
    private static final String MEDICOS_PASTA = BASE_PASTA + File.separator + "medicos";
    private static final String PACIENTES_PASTA = BASE_PASTA + File.separator + "pacientes";

    private LerArquivo() {
    }

    public static Map<String, String> lerCamposDoArquivo(String filePath) {
        Map<String, String> campos = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                // Divide a linha no primeiro ":"
                int indiceDoisPontos = linha.indexOf(": ");
                if (indiceDoisPontos > 0) {
                    String chave = linha.substring(0, indiceDoisPontos).trim();
                    String valor = linha.substring(indiceDoisPontos + 2).trim();
                    campos.put(chave, valor);
                }
            }
            return campos;
        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo " + filePath + ": " + e.getMessage());
            return null;
        }
    }

    public static Usuario buscarUsuarioPorId(int id, String tipoUsuario) {
        String diretorioBusca = "";
        if ("Medico".equalsIgnoreCase(tipoUsuario)) {
            diretorioBusca = MEDICOS_PASTA;
        } else if ("Paciente".equalsIgnoreCase(tipoUsuario)) {
            diretorioBusca = PACIENTES_PASTA;
        }

        File arquivo = new File(diretorioBusca + File.separator + id + ".txt");
        if (!arquivo.exists()) {
            return null;
        }

        Map<String, String> dados = lerCamposDoArquivo(arquivo.getAbsolutePath());
        if (dados == null) {
            return null;
        }

        try {
            if ("Medico".equalsIgnoreCase(tipoUsuario) && "Medico".equals(dados.get("Tipo"))) {
                String senha = dados.get("Senha");
                String nome = dados.get("Nome");
                String especialidade = dados.get("Especialidade");
                String planosStr = dados.get("Planos_de_Saúde_Atendidos");
                List<String> planosSaude = (planosStr != null && !planosStr.equals("Nenhum"))
                        ? Arrays.asList(planosStr.split(",")).stream().map(String::trim).collect(Collectors.toList())
                        : new ArrayList<>();

                UsuarioMedico medico = UsuarioFactory.criarMedico(senha, nome, especialidade, planosSaude);
                medico.setId(Integer.parseInt(dados.get("ID")));
                return medico;
            } else if ("Paciente".equalsIgnoreCase(tipoUsuario) && "Paciente".equals(dados.get("Tipo"))) {
                String senha = dados.get("Senha");
                String nome = dados.get("Nome");
                int idade = Integer.parseInt(dados.get("Idade"));
                String planoSaude = dados.get("Plano_de_Saúde");

                UsuarioPaciente paciente = UsuarioFactory.criarPaciente(senha, nome, idade, planoSaude);
                paciente.setId(Integer.parseInt(dados.get("ID")));
                return paciente;
            }
        } catch (NumberFormatException e) {
            System.err.println("Erro ao converter dados numéricos para o usuário ID " + id + ": " + e.getMessage());
        }
        return null;
    }

    public static UsuarioMedico buscarMedicoPorNome(String nome) {
        File medicosDir = new File(MEDICOS_PASTA);
        if (medicosDir.exists() && medicosDir.isDirectory()) {
            File[] arquivosMedicos = medicosDir.listFiles((dir, name) -> name.endsWith(".txt"));
            if (arquivosMedicos != null) {
                for (File arquivo : arquivosMedicos) {
                    Map<String, String> dados = lerCamposDoArquivo(arquivo.getAbsolutePath());
                    if (dados != null && "Medico".equals(dados.get("Tipo"))
                            && dados.get("Nome").equalsIgnoreCase(nome)) {
                        // Reconstrua o objeto Medico e retorne
                        try {
                            String senha = dados.get("Senha");
                            String especialidade = dados.get("Especialidade");
                            String planosStr = dados.get("Planos_de_Saúde_Atendidos");
                            List<String> planosSaude = (planosStr != null && !planosStr.equals("Nenhum"))
                                    ? Arrays.asList(planosStr.split(",")).stream().map(String::trim).collect(
                                            Collectors.toList())
                                    : new ArrayList<>();
                            UsuarioMedico medico = UsuarioFactory.criarMedico(senha, nome, especialidade, planosSaude);
                            medico.setId(Integer.parseInt(dados.get("ID")));
                            return medico;
                        } catch (NumberFormatException e) {
                            System.err.println("Erro ao converter ID do médico em: " + arquivo.getName());
                        }
                    }
                }
            }
        }
        return null;
    }

    public static UsuarioPaciente buscarPacientePorNome(String nome) {
        // Lógica similar para pacientes
        File pacientesDir = new File(PACIENTES_PASTA);
        if (pacientesDir.exists() && pacientesDir.isDirectory()) {
            File[] arquivosPacientes = pacientesDir.listFiles((dir, name) -> name.endsWith(".txt"));
            if (arquivosPacientes != null) {
                for (File arquivo : arquivosPacientes) {
                    Map<String, String> dados = lerCamposDoArquivo(arquivo.getAbsolutePath());
                    if (dados != null && "Paciente".equals(dados.get("Tipo"))
                            && dados.get("Nome").equalsIgnoreCase(nome)) {
                        try {
                            String senha = dados.get("Senha");
                            int idade = Integer.parseInt(dados.get("Idade"));
                            String planoSaude = dados.get("Plano_de_Saúde");

                            UsuarioPaciente paciente = UsuarioFactory.criarPaciente(senha, nome, idade, planoSaude);
                            paciente.setId(Integer.parseInt(dados.get("ID")));
                            return paciente;
                        } catch (NumberFormatException e) {
                            System.err.println("Erro ao converter idade ou ID do paciente em: " + arquivo.getName());
                        }
                    }
                }
            }
        }
        return null;
    }

    public static void listarArquivos(File diretorio, String tipo, Usuario usuario) {
        File[] arquivos = diretorio.listFiles();

        if (arquivos != null) {
            for (File arquivo : arquivos) {
                if (arquivo.isDirectory()) {
                    listarArquivos(arquivo, tipo, usuario); // Correção aqui!
                } else if (arquivo.getName().endsWith(".txt")) {
                    Map<String, String> dados = lerCamposDoArquivo(arquivo.getAbsolutePath());
                    if (dados != null) {
                        if ("medico".equalsIgnoreCase(tipo) && usuario instanceof UsuarioPaciente paciente) {
                            if (dados.get("Planos_de_Saúde_Atendidos").contains(paciente.getPlanoSaude())) {
                                System.out.println("Nome: " + dados.get("Nome"));
                                System.out.println("Especialidade: " + dados.get("Especialidade"));
                                System.out.println("Planos: " + dados.get("Planos_de_Saúde_Atendidos"));
                                System.out.println("----------------------------");
                            }

                        } else if ("consulta".equalsIgnoreCase(tipo)) {
                            if (usuario instanceof UsuarioPaciente paciente) {
                                String nomeArquivo = arquivo.getName();
                                String idNoNome = nomeArquivo.split("_")[0];

                                if (idNoNome.equals(String.valueOf(paciente.getId()))) {
                                    System.out.println("Data: " + dados.get("Data"));
                                    System.out.println("Hora: " + dados.get("Hora"));
                                    System.out.println("Médico: " + dados.get("Médico"));
                                    System.out.println("----------------------------");
                                }
                            } else if (usuario instanceof UsuarioMedico medico) {
                                String pastaMedico = String.valueOf(medico.getId());
                                if (arquivo.getParentFile().getName().equals(pastaMedico)) {
                                    System.out.println("Paciente: " + dados.get("Paciente"));
                                    System.out.println("Data: " + dados.get("Data"));
                                    System.out.println("Hora: " + dados.get("Hora"));
                                    System.out.println("----------------------------");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /*
     * public static List<Usuario> carregarTodosUsuarios() {
     * List<Usuario> usuariosCarregados = new ArrayList<>();
     * 
     * // Carregar médicos
     * File medicosDir = new File(MEDICOS_PASTA);
     * if (medicosDir.exists() && medicosDir.isDirectory()) {
     * File[] arquivosMedicos = medicosDir.listFiles((dir, name) ->
     * name.endsWith(".txt"));
     * if (arquivosMedicos != null) {
     * for (File arquivo : arquivosMedicos) {
     * Map<String, String> dados = lerCamposDoArquivo(arquivo.getAbsolutePath());
     * if (dados != null && "Medico".equals(dados.get("Tipo"))) {
     * try {
     * String senha = dados.get("Senha");
     * String nome = dados.get("Nome");
     * String especialidade = dados.get("Especialidade");
     * // Tratar "Nenhum" para planos de saúde
     * String planosStr = dados.get("Planos_de_Saúde_Atendidos");
     * List<String> planosSaude = (planosStr != null && !planosStr.equals("Nenhum"))
     * ?
     * Arrays.asList(planosStr.split(",")).stream().map(String::trim).collect(
     * Collectors.toList()) :
     * new ArrayList<>();
     * 
     * // Criamos um médico e depois setamos o ID, pois a fábrica gera um novo ID
     * // Alternativa: Se o ID fosse passado para a fábrica, seria mais direto
     * UsuarioMedico medico = UsuarioFactory.criarMedico(senha, nome, especialidade,
     * planosSaude);
     * medico.setId(Integer.parseInt(dados.get("ID"))); // Setamos o ID lido do
     * arquivo
     * usuariosCarregados.add(medico);
     * } catch (NumberFormatException e) {
     * System.err.println("Erro ao converter ID do médico em: " +
     * arquivo.getName());
     * }
     * }
     * }
     * }
     * }
     * 
     * // Carregar pacientes
     * File pacientesDir = new File(PACIENTES_PASTA);
     * if (pacientesDir.exists() && pacientesDir.isDirectory()) {
     * File[] arquivosPacientes = pacientesDir.listFiles((dir, name) ->
     * name.endsWith(".txt"));
     * if (arquivosPacientes != null) {
     * for (File arquivo : arquivosPacientes) {
     * Map<String, String> dados = lerCamposDoArquivo(arquivo.getAbsolutePath());
     * if (dados != null && "Paciente".equals(dados.get("Tipo"))) {
     * try {
     * String senha = dados.get("Senha");
     * String nome = dados.get("Nome");
     * int idade = Integer.parseInt(dados.get("Idade"));
     * String planoSaude = dados.get("Plano_de_Saúde");
     * 
     * // Criamos um paciente e depois setamos o ID
     * UsuarioPaciente paciente = UsuarioFactory.criarPaciente(senha, nome, idade,
     * planoSaude);
     * paciente.setId(Integer.parseInt(dados.get("ID"))); // Setamos o ID lido do
     * arquivo
     * usuariosCarregados.add(paciente);
     * } catch (NumberFormatException e) {
     * System.err.println("Erro ao converter idade ou ID do paciente em: " +
     * arquivo.getName());
     * }
     * }
     * }
     * }
     * }
     * return usuariosCarregados;
     * }
     */
}