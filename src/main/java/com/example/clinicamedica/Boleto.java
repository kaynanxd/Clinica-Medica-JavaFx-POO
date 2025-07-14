package com.example.clinicamedica;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Boleto {
    private String codigo;
    private double valor;
    private LocalDate dataVencimento;
    private boolean pago;
    private int idPaciente;
    private String nomeMedico;
    private String especialidade;
    String vencimento;

    public Boleto(double valor, int idPaciente, String nomeMedico, String especialidade) {
        this.codigo = "BLT" + System.currentTimeMillis();
        this.valor = valor;
        this.dataVencimento = LocalDate.now().plusDays(7);
        this.pago = false;
        this.idPaciente = idPaciente;
        this.nomeMedico = nomeMedico;
        this.especialidade = especialidade;
    }

    public void marcarComoPago() {
        this.pago = true;
    }

    public String gerarConteudoBoleto() {
        DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return "=== BOLETO MÉDICO ===\n" +
                "Código: " + codigo + "\n" +
                "Valor: R$" + String.format("%.2f", valor) + "\n" +
                "Vencimento: " + dataVencimento.format(formatoData) + "\n" +
                "Paciente ID: " + idPaciente + "\n" +
                "Médico: " + nomeMedico + "\n" +
                "Especialidade: " + especialidade + "\n" +
                "Status: " + (pago ? "PAGO" : "PENDENTE");
    }
    // Getters
    public String getCodigo() { return codigo; }
    public boolean isPago() { return pago; }
    public double getValor() { return valor; }
    public int getIdPaciente() { return idPaciente; }

    public String getDataVencimento() {
        return dataVencimento.format(DateTimeFormatter.ISO_DATE);
    }
    public String getNomeMedico() {
        return nomeMedico;
    }
    public String getEspecialidade() {
        return especialidade;
    }
}