package com.example.clinicamedica;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ConfiguracaoValores {
    private static final Map<String, Double> valoresPorEspecialidade = new HashMap<>();

    static {
        carregarValores();
    }

    private static void carregarValores() {
        File arquivo = new File("configs/config_valores.txt");

        try (Scanner scanner = new Scanner(arquivo)) {
            while (scanner.hasNextLine()) {
                String linha = scanner.nextLine().trim();
                if (linha.isEmpty() || linha.startsWith("#")) continue;

                String[] partes = linha.split("=");
                if (partes.length == 2) {
                    try {
                        String especialidade = partes[0].trim().toLowerCase();
                        double valor = Double.parseDouble(partes[1].trim());
                        valoresPorEspecialidade.put(especialidade, valor);
                    } catch (NumberFormatException e) {
                        System.err.println("Formato inválido ");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao ler arquivo de configuração: " + e.getMessage());
        }
    }

    public static double getValor(String especialidade) {
        if (especialidade == null) return 200.00;
        return valoresPorEspecialidade.getOrDefault(especialidade.toLowerCase(), 200.00);
    }

}