package com.example.clinicamedica;

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
    private static final String MEDICOS_PASTA = BASE_PASTA + "/medicos";
    private static final String PACIENTES_PASTA = BASE_PASTA  + "/pacientes";

    private LerArquivo() {
    }



    public static Map<String, String> lerCamposDoArquivo(String filePath) {
        Map<String, String> campos = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
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

        File arquivo = new File(diretorioBusca + "/" + id + ".txt");
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
            System.err.println("Erro " + e.getMessage());
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
                            System.err.println("Erro ");
                        }
                    }
                }
            }
        }
        return null;
    }

    public static UsuarioPaciente buscarPacientePorNome(String nome) {
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
                            System.err.println("Erro ");
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
                    listarArquivos(arquivo, tipo, usuario);
                } else if (arquivo.getName().endsWith(".txt")) {
                    Map<String, String> dados = lerCamposDoArquivo(arquivo.getAbsolutePath());
                    if (dados != null) {
                        if ("medico".equalsIgnoreCase(tipo) && usuario instanceof UsuarioPaciente paciente) {
                            if (dados.get("Planos_de_Saúde_Atendidos").contains(paciente.getPlanoSaude())) {
                                System.out.println("Nome: " + dados.get("Nome"));
                                System.out.println("Especialidade: " + dados.get("Especialidade"));
                                System.out.printf("Avaliação Média: %.1f estrelas\n", calcularMediaAvaliacoesMedico(Integer.parseInt(dados.get("ID"))));
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

    public static double calcularMediaAvaliacoesMedico(int medicoId) {
        String pastaAvaliacoes = "dados_avaliacoes/" + medicoId + "/";
        File diretorio = new File(pastaAvaliacoes);
        if (!diretorio.exists() || !diretorio.isDirectory()) {
            return 0.0;
        }

        File[] arquivosAvaliacao = diretorio.listFiles((dir, name) -> name.endsWith("_avaliacao.txt"));
        if (arquivosAvaliacao == null || arquivosAvaliacao.length == 0) {
            return 0.0;
        }

        double totalEstrelas = 0;
        int numAvaliacoes = 0;

        for (File arquivo : arquivosAvaliacao) {
            Map<String, String> dados = lerCamposDoArquivo(arquivo.getAbsolutePath());
            if (dados != null && dados.containsKey("Notas")) {
                try {
                    String notasStr = dados.get("Notas");
                    double estrelas = 0.0;
                    if (notasStr.contains("⭐")) {
                        estrelas = notasStr.chars().filter(ch -> ch == '⭐').count();
                        if (notasStr.contains("½")) {
                            estrelas += 0.5;
                        }
                    } else {
                        estrelas = Double.parseDouble(notasStr);
                    }
                    totalEstrelas += estrelas;
                    numAvaliacoes++;
                } catch (NumberFormatException e) {
                    System.err.println("Erro ");
                }
            }
        }
        return numAvaliacoes > 0 ? totalEstrelas / numAvaliacoes : 0.0;
    }

    public static List<String> getListaNomesMedicos(UsuarioPaciente paciente) {
        List<String> nomesMedicos = new ArrayList<>();
        File medicosDir = new File(MEDICOS_PASTA);

        if (medicosDir.exists() && medicosDir.isDirectory()) {
            File[] arquivosMedicos = medicosDir.listFiles((dir, name) -> name.endsWith(".txt"));
            if (arquivosMedicos != null) {
                for (File arquivo : arquivosMedicos) {
                    Map<String, String> dados = lerCamposDoArquivo(arquivo.getAbsolutePath());
                    if (dados != null && "Medico".equalsIgnoreCase(dados.get("Tipo"))) {
                        String planosStr = dados.get("Planos_de_Saúde_Atendidos");
                        if (planosStr != null && planosStr.contains(paciente.getPlanoSaude())) {
                            nomesMedicos.add(dados.get("Nome"));
                        }
                    }
                }
            }
        }
        return nomesMedicos;
    }
}