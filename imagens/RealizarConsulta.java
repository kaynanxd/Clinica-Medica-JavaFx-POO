package com.example.clinicamedica;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class RealizarConsulta {
    private UsuarioMedico medico;
    private static Map<String, Boleto> boletosPendentes = new HashMap<>();

    public RealizarConsulta(UsuarioMedico medico) {
        this.medico = medico;
    }

    public static Boleto gerarBoletoComEspecialidadePreco(UsuarioMedico medico, UsuarioPaciente paciente) {
        double valor = ConfiguracaoValores.getValor(medico.getEspecialidade());
        Boleto novoBoleto = new Boleto(valor, paciente.getId(), medico.getNome(), medico.getEspecialidade());
        boletosPendentes.put(novoBoleto.getCodigo(), novoBoleto);
        return novoBoleto;
    }

    public void realizarConsulta(String dataInput, String horarioConsulta, String idPaciente,
                                 String sintomas, String tratamentoSugerido, String exames, String medicamentos) {
        System.out.print("\n=== Realizar Consulta ===\n");
        System.out.print("\n --- Consultas Marcadas ---\n");

        String caminho = "dados_agendamentos/" + medico.getId() + "/";
        File pasta = new File(caminho);
        LerArquivo.listarArquivos(pasta, "consulta", medico);

        // Formatar data para nome de arquivo
        DateTimeFormatter formatoEntrada = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatoArquivo = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate data = LocalDate.parse(dataInput, formatoEntrada);
        String dataConsulta = data.format(formatoArquivo);

        LocalTime hora = LocalTime.parse(horarioConsulta);

        int idPacienteInt;
        try {
            idPacienteInt = Integer.parseInt(idPaciente);
        } catch (NumberFormatException e) {
            System.out.println("ID do paciente inválido.");
            return;
        }

        UsuarioPaciente paciente = (UsuarioPaciente) LerArquivo.buscarUsuarioPorId(idPacienteInt, "Paciente");
        if (paciente == null) {
            System.out.println("Paciente não encontrado.");
            return;
        }

        LocalDateTime dataHoraConsulta = LocalDateTime.of(data, hora);

        Consulta consulta = new Consulta(medico, paciente, dataHoraConsulta, StatusConsulta.AGENDADA);
        consulta.atualizarStatusNaConsulta(StatusConsulta.REALIZADA);

        String conteudo = "Paciente ID: " + idPaciente +
                "\nData: " + dataConsulta +
                "\nHorário: " + horarioConsulta +
                "\nSintomas: " + sintomas +
                "\nTratamento sugerido: " + tratamentoSugerido +
                "\nExames: " + exames +
                "\nMedicamentos: " + medicamentos +
                "\nStatus: REALIZADA";

        String nomeArquivoRelatorio = idPaciente + "_" + dataConsulta + "_" + horarioConsulta.replace(":", "-") + "_relatorio.txt";
        String pastaRelatorio = "dados_consultas/" + medico.getId() + "/";
        new File(pastaRelatorio).mkdirs();
        EscreverArquivo.escreverEmArquivo(nomeArquivoRelatorio, pastaRelatorio, conteudo);

        System.out.println("Consulta registrada com sucesso.");

        // Libera o horário movendo o arquivo original da consulta para histórico
        String nomeArquivoConsulta = idPaciente + "_" + dataConsulta + "_" + horarioConsulta.replace(":", "-") + ".txt";
        Path origem = Paths.get("dados_agendamentos/" + medico.getId(), nomeArquivoConsulta);
        Path destino = Paths.get("dados_realizadas/" + medico.getId(), nomeArquivoConsulta);

        try {
            Files.createDirectories(destino.getParent());
            if (Files.exists(origem)) {
                Files.move(origem, destino, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Consulta marcada como realizada e movida para histórico.");
            } else {
                System.out.println("Arquivo da consulta não encontrado para mover.");
            }
        } catch (IOException e) {
            System.err.println("Erro ao mover consulta para histórico: " + e.getMessage());
        }

        // Geração de boleto se o paciente não tem plano
        if (paciente.getPlanoSaude().equalsIgnoreCase("Sem")) {
            Boleto boleto = gerarBoletoComEspecialidadePreco(medico, paciente);
            String conteudoBoleto = boleto.gerarConteudoBoleto();

            String nomeArquivoBoleto = "boleto_" + boleto.getCodigo() + ".txt";
            String pastaBoletos = "dados_boletos/" + paciente.getId() + "/";
            new File(pastaBoletos).mkdirs();

            EscreverArquivo.escreverEmArquivo(nomeArquivoBoleto, pastaBoletos, conteudoBoleto);
            System.out.println("Boleto gerado: " + boleto.getCodigo());
        }
    }

    public void visualizarConsultas() {
        System.out.print("\n=== Visualizar Consultas ===\n");
        System.out.print("\n --- Consultas Marcadas ---\n");
        String caminho = "dados_agendamentos/" + medico.getId() + "/";
        File pasta = new File(caminho);
        LerArquivo.listarArquivos(pasta, "consulta", medico);
    }
}
