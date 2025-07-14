package com.example.clinicamedica;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class AgendadordeConsulta {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final ReentrantLock fileLock = new ReentrantLock();

    private final List<Consulta> consultas;
    private final UsuarioPaciente pacienteLogado;
    private final Map<String, List<UsuarioPaciente>> listaEspera = new HashMap<>();

    public AgendadordeConsulta(UsuarioPaciente pacienteLogado) {
        this.consultas = new ArrayList<>();
        this.pacienteLogado = pacienteLogado;
    }

    public void agendarConsulta(String nomeMedico, int escolhaData, int escolhaHora) {
        fileLock.lock();
        try {
            UsuarioMedico medico = LerArquivo.buscarMedicoPorNome(nomeMedico);
            if (medico == null) {
                System.out.println("Médico não encontrado.");
                return;
            }

            LocalDate[] datasConsulta = {
                    LocalDate.now(),
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(2)
            };

            if (escolhaData < 1 || escolhaData > 3) {
                System.out.println("Data inválida");
                return;
            }

            LocalDate dataEscolhida = datasConsulta[escolhaData - 1];

            Set<LocalTime> horariosOcupados = new HashSet<>();
            buscarHorariosOcupados(medico, dataEscolhida, horariosOcupados);

            LocalTime[] horariosConsultas = {
                    LocalTime.of(9, 0),
                    LocalTime.of(12, 0),
                    LocalTime.of(15, 0)
            };

            List<LocalTime> horariosDisponiveis = new ArrayList<>();
            for (LocalTime horario : horariosConsultas) {
                if (!horariosOcupados.contains(horario)) {
                    horariosDisponiveis.add(horario);
                }
            }

            if (horariosDisponiveis.isEmpty()) {
                adicionarListaEspera(dataEscolhida);
                return;
            }

            if (escolhaHora < 1 || escolhaHora > horariosDisponiveis.size()) {
                System.out.println("Horário inválido.");
                return;
            }

            LocalTime horaEscolhida = horariosDisponiveis.get(escolhaHora - 1);
            finalizarAgendamento(medico, dataEscolhida, horaEscolhida);
        } finally {
            fileLock.unlock();
        }
    }

    private void buscarHorariosOcupados(UsuarioMedico medico, LocalDate data, Set<LocalTime> horariosOcupados) {
        for (Agendamento ag : medico.getAgendamentos()) {
            if (ag.getData().equals(data)) {
                horariosOcupados.add(ag.getHora());
            }
        }

        Path pastaMedico = Paths.get("dados_agendamentos/" + medico.getId());
        if (!Files.exists(pastaMedico)) {
            return;
        }

        try {
            Files.list(pastaMedico)
                    .filter(path -> path.getFileName().toString().contains(data.toString()))
                    .forEach(path -> {
                        String nomeArquivo = path.getFileName().toString();
                        String[] partes = nomeArquivo.split("_");
                        if (partes.length >= 3) {
                            String horaParte = partes[2].replace(".txt", "").replace("-", ":");
                            try {
                                horariosOcupados.add(LocalTime.parse(horaParte));
                            } catch (Exception e) {
                                System.err.println("Erro ao parsear hora do arquivo: " + nomeArquivo);
                            }
                        }
                    });
        } catch (IOException e) {
            System.err.println("Erro ao listar arquivos: " + e.getMessage());
        }
    }

    private void adicionarListaEspera(LocalDate data) {
        String chave = data.toString();
        listaEspera.putIfAbsent(chave, new ArrayList<>());
        listaEspera.get(chave).add(pacienteLogado);
        System.out.println("Todos os horários estão ocupados. Você foi adicionado à lista de espera para " + chave);
    }

    private void finalizarAgendamento(UsuarioMedico medico, LocalDate data, LocalTime hora) {
        try {
            Path pastaMedico = Paths.get("dados_agendamentos/" + medico.getId());
            Files.createDirectories(pastaMedico);

            String nomeArquivo = formatarNomeArquivo(data, hora);
            Path caminhoArquivo = pastaMedico.resolve(nomeArquivo);

            Files.createFile(caminhoArquivo);

            Agendamento novo = new Agendamento(null, data, hora);
            medico.adicionarAgendamento(novo);
            EscreverArquivo.escreverAgendamento(medico, pacienteLogado, data, hora,StatusConsulta.AGENDADA);

            Consulta novaConsulta = new Consulta(
                    medico,
                    pacienteLogado,
                    LocalDateTime.of(data, hora),
                    StatusConsulta.AGENDADA);
            consultas.add(novaConsulta);

            System.out.println("Consulta marcada com sucesso!");
        } catch (IOException e) {
            System.err.println("Erro ao finalizar agendamento: " + e.getMessage());
        }
    }

    public void cancelarConsulta(String dataStr, String horarioStr, String nomeMedico) {
        fileLock.lock();
        try {
            UsuarioMedico medico = LerArquivo.buscarMedicoPorNome(nomeMedico);
            if (medico == null) {
                System.out.println("Médico não encontrado.");
                return;
            }

            LocalDate data = LocalDate.parse(dataStr, DATE_FORMATTER);
            LocalTime hora = LocalTime.parse(horarioStr);
            String dataFormatada = data.format(FILE_DATE_FORMATTER);
            String horaFormatada = hora.format(DateTimeFormatter.ofPattern("HH-mm"));

            String nomeArquivo = pacienteLogado.getId() + "_" + dataFormatada + "_" + horaFormatada + ".txt";

            Path pastaAgendamentos = Paths.get("dados_agendamentos/" + medico.getId());
            Path pastaCancelados = Paths.get("dados_cancelados/" + medico.getId());

            Files.createDirectories(pastaAgendamentos);
            Files.createDirectories(pastaCancelados);

            Path arquivoOriginal = pastaAgendamentos.resolve(nomeArquivo);
            Path arquivoHistorico = pastaCancelados.resolve(nomeArquivo);

            if (Files.exists(arquivoOriginal)) {
                List<String> linhas = Files.readAllLines(arquivoOriginal);
                for (int i = 0; i < linhas.size(); i++) {
                    if (linhas.get(i).startsWith("Status:")) {
                        linhas.set(i, "Status: CANCELADA");
                        break;
                    }
                }
                Files.write(arquivoOriginal, linhas);

                // Move o arquivo para a pasta de cancelados
                Files.move(arquivoOriginal, arquivoHistorico, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Consulta cancelada e movida para histórico.");

                notificarListaEspera(dataFormatada);
            } else {
                System.out.println("Arquivo da consulta não encontrado: " + arquivoOriginal);
            }
        } catch (Exception e) {
            System.err.println("Erro ao cancelar consulta: " + e.getMessage());
            e.printStackTrace();
        } finally {
            fileLock.unlock();
        }
    }

    public List<LocalDate> obterDatasDisponiveis() {
        return List.of(
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2)
        );
    }

    public List<LocalTime> obterHorariosDisponiveis(String nomeMedico, LocalDate data) {
        UsuarioMedico medico = LerArquivo.buscarMedicoPorNome(nomeMedico);
        if (medico == null) return List.of();

        Set<LocalTime> horariosOcupados = new HashSet<>();
        buscarHorariosOcupados(medico, data, horariosOcupados);

        LocalTime[] horariosPadrao = {
                LocalTime.of(9, 0),
                LocalTime.of(12, 0),
                LocalTime.of(15, 0)
        };

        List<LocalTime> horariosDisponiveis = new ArrayList<>();
        for (LocalTime h : horariosPadrao) {
            if (!horariosOcupados.contains(h)) {
                horariosDisponiveis.add(h);
            }
        }

        return horariosDisponiveis;
    }

    private void notificarListaEspera(String data) {
        if (listaEspera.containsKey(data) && !listaEspera.get(data).isEmpty()) {
            UsuarioPaciente proximo = listaEspera.get(data).remove(0);
            System.out.println("Paciente " + proximo.getNome() + " foi notificado sobre a vaga.");
        }
    }

    public List<Consulta> getConsultasFiltradasPorStatus(Set<StatusConsulta> statusPermitidos) {
        List<Consulta> consultas = new ArrayList<>();
        File pasta = new File("dados_agendamentos");

        if (!pasta.exists()) return consultas;

        for (File medicoFolder : Objects.requireNonNull(pasta.listFiles())) {
            if (medicoFolder.isDirectory()) {
                for (File arq : Objects.requireNonNull(medicoFolder.listFiles())) {
                    if (arq.getName().startsWith(pacienteLogado.getId() + "_")) {
                        try {
                            List<String> linhas = Files.readAllLines(arq.toPath());
                            Optional<String> statusLinha = linhas.stream()
                                    .filter(l -> l.startsWith("Status: "))
                                    .findFirst();

                            if (statusLinha.isEmpty()) continue;

                            String statusStr = statusLinha.get().replace("Status: ", "").trim();
                            StatusConsulta status = StatusConsulta.valueOf(statusStr);

                            if (!statusPermitidos.contains(status)) continue;

                            String[] partes = arq.getName().replace(".txt", "").split("_");
                            String dataStr = partes[1];
                            String horaStr = partes[2].replace("-", ":");

                            LocalDate data = LocalDate.parse(dataStr);
                            LocalTime hora = LocalTime.parse(horaStr);

                            UsuarioMedico medico = (UsuarioMedico) LerArquivo.buscarUsuarioPorId(
                                    Integer.parseInt(medicoFolder.getName()), "Medico");

                            consultas.add(new Consulta(medico, pacienteLogado,
                                    LocalDateTime.of(data, hora), status));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        return consultas;
    }


    public void exibirMedicos() {
        LerArquivo.listarArquivos(new File("dados_usuarios/medicos"), "medico", pacienteLogado);
    }

    private String formatarNomeArquivo(LocalDate data, LocalTime hora) {
        return pacienteLogado.getId() + "_" +
                data.format(FILE_DATE_FORMATTER) + "_" +
                hora.toString().replace(":", "-") + ".txt";
    }
    public List<String> getListaNomesMedicos() {
        return LerArquivo.getListaNomesMedicos(pacienteLogado);
    }
}