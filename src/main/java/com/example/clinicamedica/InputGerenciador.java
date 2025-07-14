package com.example.clinicamedica;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class InputGerenciador {
    private static final Scanner scanner = new Scanner(System.in);

    private InputGerenciador() {}

    public static String lerString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public static int lerInteiro(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = scanner.nextInt();
                scanner.nextLine();
                return value;
            } catch (InputMismatchException e) {
                System.out.println("Por favor, digite um número inteiro.");
                scanner.nextLine();
            }
        }
    }

    public static List<String> lerLista(String prompt, String delimitador) {
        System.out.print(prompt);
        String input = scanner.nextLine();
        List<String> list = new ArrayList<>();
        if (!input.trim().isEmpty()) {
            String[] items = input.split(delimitador);
            for (String item : items) {
                list.add(item.trim());
            }
        }
        return list;
    }

    public static void closeScanner() {
        scanner.close();
    }
}