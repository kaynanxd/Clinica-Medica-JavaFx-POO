package com.example.clinicamedica;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Consulta {
    private UsuarioMedico medico;
    private UsuarioPaciente paciente;
    private LocalDateTime dataHoraConsulta;
    private StatusConsulta statusConsulta;

    public Consulta(UsuarioMedico medico, UsuarioPaciente paciente, LocalDateTime dataHoraConsulta, StatusConsulta statusConsulta) {
        this.medico = medico;
        this.paciente = paciente;
        this.dataHoraConsulta = dataHoraConsulta;
        this.statusConsulta = statusConsulta;
    }

    //gets
    public UsuarioPaciente getPaciente() {
        return paciente;
    }
    public UsuarioMedico getMedico() {
        return medico;
    }
    public LocalDateTime getDataHoraConsulta() {
        return dataHoraConsulta;
    }
    public StatusConsulta getStatusConsulta(){return statusConsulta;}

    //sets
    public void setPaciente(UsuarioPaciente paciente) {
        this.paciente = paciente;
    }
    public void setMedico(UsuarioMedico medico) {
        this.medico = medico;
    }
    public void setDataHoraConsulta(LocalDateTime dataHoraConsulta) {
        this.dataHoraConsulta = dataHoraConsulta;
    }
    public void setStatusConsulta(StatusConsulta statusConsulta) {
        this.statusConsulta = statusConsulta;
    }

    public void atualizarStatusNaConsulta(StatusConsulta novoStatus) {
        String pasta = "dados_agendamentos/" + medico.getId() + "/";
        String nomeArquivo = paciente.getId() + "_" +
                dataHoraConsulta.toLocalDate() + "_" +
                dataHoraConsulta.toLocalTime().toString().replace(":", "-") + ".txt";

        File arquivo = new File(pasta + nomeArquivo);

        if (!arquivo.exists()) {
            System.out.println("Arquivo da consulta não encontrado.");
            return;
        }

        List<String> linhas = new ArrayList<>();
        try (Scanner leitor = new Scanner(arquivo)) {
            while (leitor.hasNextLine()) {
                String linha = leitor.nextLine();
                if (linha.startsWith("Status:")) {
                    linhas.add("Status: " + novoStatus);
                } else {
                    linhas.add(linha);
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo da consulta.");
            return;
        }

        try (FileWriter writer = new FileWriter(arquivo, false)) { // sobrescreve
            for (String linha : linhas) {
                writer.write(linha + "\n");
            }
            this.statusConsulta = novoStatus;
            System.out.println("Status da consulta atualizado para " + novoStatus);
        } catch (IOException e) {
            System.out.println("Erro ao escrever");
        }
    }
}
