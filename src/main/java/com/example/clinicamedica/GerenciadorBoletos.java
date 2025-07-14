package com.example.clinicamedica;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GerenciadorBoletos {
    private  final List<Boleto> boletos = new ArrayList<>();

    public List<Boleto> listarBoletosPendentes(int idPaciente) {
        return boletos.stream()
                .filter(b -> b.getIdPaciente() == idPaciente && !b.isPago())
                .collect(Collectors.toList());
    }

    public Boleto buscarBoleto(String codigo, int idPaciente) {
        return boletos.stream()
                .filter(b -> b.getCodigo().equals(codigo) && b.getIdPaciente() == idPaciente)
                .findFirst()
                .orElse(null);
    }

    public void marcarComoPago(String codigo) {
        boletos.stream()
                .filter(b -> b.getCodigo().equals(codigo))
                .findFirst()
                .ifPresent(Boleto::marcarComoPago);
    }

    public List<Boleto> getTodosBoletosPaciente(int idPaciente) {
        return boletos.stream()
                .filter(b -> b.getIdPaciente() == idPaciente)
                .collect(Collectors.toList());
    }

    static void atualizarStatusBoleto(UsuarioPaciente paciente, String codigoBoleto) {
        String pastaBoletos = "dados_boletos/" + paciente.getId() + "/";
        String nomeArquivo = "boleto_" + codigoBoleto + ".txt";
        File arquivoBoleto = new File(pastaBoletos + nomeArquivo);

        if (!arquivoBoleto.exists()) {
            System.out.println("Arquivo do boleto não encontrado");
            return;
        }

        try {
            List<String> linhas = Files.readAllLines(arquivoBoleto.toPath());
            List<String> novasLinhas = new ArrayList<>();

            for (String linha : linhas) {
                if (linha.startsWith("Status:")) {
                    novasLinhas.add("Status: PAGO");
                } else {
                    novasLinhas.add(linha);
                }
            }

            Files.write(arquivoBoleto.toPath(), novasLinhas);
            System.out.println("Status do boleto atualizado!");
        } catch (IOException e) {
            System.out.println("Erro ao atualizar boleto: " + e.getMessage());
        }
    }
    public void carregarBoletosPaciente(int idPaciente) {
        boletos.clear();
        String pastaBoletos = "dados_boletos/" + idPaciente + "/";
        File diretorio = new File(pastaBoletos);

        if (!diretorio.exists() || !diretorio.isDirectory()) {
            return;
        }

        File[] arquivos = diretorio.listFiles((dir, name) -> name.startsWith("boleto_") && name.endsWith(".txt"));

        if (arquivos == null || arquivos.length == 0) {
            return;
        }

        for (File arquivo : arquivos) {
            try {
                List<String> linhas = Files.readAllLines(arquivo.toPath());
                String codigo = "";
                double valor = 0;
                LocalDate dataVencimento = null;
                boolean pago = false;
                int idPacienteArquivo = idPaciente;
                String nomeMedico = "";
                String especialidade = "";

                for (String linha : linhas) {
                    if (linha.startsWith("Código: ")) {
                        codigo = linha.substring("Código: ".length()).trim();
                    } else if (linha.startsWith("Valor: R$")) {
                    String valorStr = linha.substring("Valor: R$".length()).trim().replace(",", ".");
                    valor = Double.parseDouble(valorStr);
                }
                else if (linha.startsWith("Vencimento: ")) {
                        DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        dataVencimento = LocalDate.parse(linha.substring("Vencimento: ".length()).trim(), formatoData);
                    } else if (linha.startsWith("Status: ")) {
                        pago = linha.substring("Status: ".length()).trim().equalsIgnoreCase("PAGO");
                    } else if (linha.startsWith("Médico: ")) {
                        nomeMedico = linha.substring("Médico: ".length()).trim();
                    } else if (linha.startsWith("Especialidade: ")) {
                        especialidade = linha.substring("Especialidade: ".length()).trim();
                    }
                }

                Boleto boleto = new Boleto(valor, idPacienteArquivo, nomeMedico, especialidade);
                setField(boleto, "codigo", codigo);
                setField(boleto, "dataVencimento", dataVencimento);
                setField(boleto, "pago", pago);

                boletos.add(boleto);

            } catch (Exception e) {
                System.err.println("Erro ao carregar boleto )");
                e.printStackTrace();
            }
        }
    }

    private void setField(Boleto boleto, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = Boleto.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(boleto, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}