package com.example.clinicamedica;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class Avaliacao {
    private UsuarioPaciente paciente;

    public Avaliacao(UsuarioPaciente paciente) {
        this.paciente = paciente;
    }

    public void avaliarConsulta(String dataConsultaStr, String horarioConsultaStr,
                                String nomeMedico, String textoAvaliacao, double estrelasEscolhidas) {
        System.out.print("\n=== Avaliar Consulta ===\n");

        UsuarioMedico medico = LerArquivo.buscarMedicoPorNome(nomeMedico);
        if (medico == null) {
            System.out.println("Médico não encontrado.");
            return;
        }

        DateTimeFormatter formatoEntradaData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate dataConsulta = LocalDate.parse(dataConsultaStr, formatoEntradaData);
        LocalTime horarioConsulta = LocalTime.parse(horarioConsultaStr);
        LocalDateTime dataHoraConsulta = LocalDateTime.of(dataConsulta, horarioConsulta);

        // Verificar se a consulta existe e se o status é REALIZADA
        String nomeArquivoAgendamento = paciente.getId() + "_" +
                dataConsulta.toString() + "_" +
                horarioConsultaStr.replace(":", "-") + ".txt";
        File arquivoAgendamento = new File("dados_agendamentos/" + medico.getId() + "/" + nomeArquivoAgendamento);

        if (!arquivoAgendamento.exists()) {
            System.out.println("Consulta não encontrada para avaliação.");
            return;
        }

        Map<String, String> dadosConsulta = LerArquivo.lerCamposDoArquivo(arquivoAgendamento.getAbsolutePath());


        if (dadosConsulta == null || !dadosConsulta.containsKey("Status")) {
            System.out.println("Não foi possível verificar o status da consulta.");
            return;
        }

        StatusConsulta statusAtual = StatusConsulta.valueOf(dadosConsulta.get("Status"));

        if (statusAtual != StatusConsulta.REALIZADA) {
            System.out.println("A consulta só pode ser avaliada após ser REALIZADA. Status atual: " + statusAtual);
            return;
        }

        String conteudo = "Usuário: " + paciente.getNome() +
                "\nTexto: " + textoAvaliacao +
                "\nNotas: " + getEstrelasString(estrelasEscolhidas);

        String dataFormatada = dataConsultaStr.replace("/", "-");
        String horarioFormatado = horarioConsultaStr.replace(":", "-");
        String nomeArquivoRelatorio = medico.getId() + "_" + dataFormatada + "_" + horarioFormatado + "_avaliacao.txt";

        String pastaRelatorio = "dados_avaliacoes/" + medico.getId() + "/";
        new File(pastaRelatorio).mkdirs();
        EscreverArquivo.escreverEmArquivo(nomeArquivoRelatorio, pastaRelatorio, conteudo);
        dadosConsulta.put("Status", "CONCLUIDA");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoAgendamento))) {
            for (Map.Entry<String, String> entry : dadosConsulta.entrySet()) {
                writer.write(entry.getKey() + ": " + entry.getValue());
                writer.newLine();
            }
            System.out.println("Avaliação registrada com sucesso e status da consulta atualizado para CONCLUIDA.");
        } catch (IOException e) {
            System.err.println("Erro ao atualizar o status da consulta: " + e.getMessage());
        }
        System.out.println("Avaliação registrada com sucesso.");
    }

    public void visualizarConsultas() {
        String caminho = "dados_agendamentos/";
        File pasta = new File(caminho);
        LerArquivo.listarArquivos(pasta, "consulta", paciente);
    }

    public static String getEstrelasString(double nota) {
        int estrelaCheia = (int) nota;
        boolean meiaEstrela = (nota - estrelaCheia) >= 0.5;
        StringBuilder estrelas = new StringBuilder();

        for (int i = 0; i < estrelaCheia; i++)
            estrelas.append("⭐");
        if (meiaEstrela)
            estrelas.append("½");

        return estrelas.toString();
    }
}