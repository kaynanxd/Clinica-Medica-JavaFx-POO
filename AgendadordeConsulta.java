import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AgendadordeConsulta {

    private final List<Consulta> consultas;
    private final UsuarioPaciente pacienteLogado;
    private final Map<String, List<UsuarioPaciente>> listaEspera = new HashMap<>();

    public AgendadordeConsulta(UsuarioPaciente pacienteLogado) {
        this.consultas = new ArrayList<>();
        this.pacienteLogado = pacienteLogado;
    }

    public void agendarConsulta(String nomeMedico, int escolhaData, int escolhaHora) {
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
    }

    private void buscarHorariosOcupados(UsuarioMedico medico, LocalDate data, Set<LocalTime> horariosOcupados) {
        for (Agendamento ag : medico.getAgendamentos()) {
            if (ag.getData().equals(data)) {
                horariosOcupados.add(ag.getHora());
            }
        }

        File pastaMedico = new File("dados_agendamentos/" + medico.getId() + "/");
        if (pastaMedico.exists()) {
            for (File arquivo : pastaMedico.listFiles()) {
                if (arquivo.getName().contains(data.toString())) {
                    String[] partes = arquivo.getName().split("_");
                    String horaParte = partes[2].replace(".txt", "").replace("-", ":");
                    horariosOcupados.add(LocalTime.parse(horaParte));
                }
            }
        }
    }

    private void adicionarListaEspera(LocalDate data) {
        String chave = data.toString();
        listaEspera.putIfAbsent(chave, new ArrayList<>());
        listaEspera.get(chave).add(pacienteLogado);
        System.out.println("Todos os horários estão ocupados. Você foi adicionado à lista de espera para " + chave);
    }

    private void finalizarAgendamento(UsuarioMedico medico, LocalDate data, LocalTime hora) {
        Agendamento novo = new Agendamento(null, data, hora);
        medico.adicionarAgendamento(novo);
        EscreverArquivo.escreverAgendamento(medico, pacienteLogado, data, hora);

        Consulta novaConsulta = new Consulta(
                medico,
                pacienteLogado,
                LocalDateTime.of(data, hora),
                StatusConsulta.AGENDADA);
        consultas.add(novaConsulta);

        System.out.println("Consulta marcada com sucesso!");
    }

    public void cancelarConsulta(String dataCancelar, String horarioCancelar, String nomeMedico) {
        UsuarioMedico medico = LerArquivo.buscarMedicoPorNome(nomeMedico);
        if (medico == null) {
            System.out.println("Médico não encontrado.");
            return;
        }

        DateTimeFormatter formatoEntrada = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate data = LocalDate.parse(dataCancelar, formatoEntrada);
        LocalTime hora = LocalTime.parse(horarioCancelar);

        Consulta consulta = new Consulta(
                medico,
                pacienteLogado,
                LocalDateTime.of(data, hora),
                StatusConsulta.AGENDADA);
        consulta.atualizarStatusNoArquivo(StatusConsulta.CANCELADA);

        String dataFormatada = data.toString();
        String horaFormatada = horarioCancelar.replace(":", "-");
        String nomeArquivo = pacienteLogado.getId() + "_" + dataFormatada + "_" + horaFormatada + ".txt";

        File arquivoOriginal = new File("dados_agendamentos/" + medico.getId() + "/" + nomeArquivo);
        File arquivoHistorico = new File("dados_cancelados/" + medico.getId() + "/" + nomeArquivo);

        if (arquivoOriginal.exists()) {
            if (arquivoOriginal.renameTo(arquivoHistorico)) {
                System.out.println("Consulta cancelada e movida para histórico.");
            } else {
                System.out.println("Consulta cancelada, mas não foi possível mover para histórico.");
            }
        } else {
            System.out.println("Arquivo da consulta não encontrado.");
        }

        notificarListaEspera(dataFormatada);
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

    public void exibirMedicos() {
        LerArquivo.listarArquivos(new File("dados_usuarios/medicos"), "medico", pacienteLogado);
    }

    public void exibirConsultas() {
        LerArquivo.listarArquivos(new File("dados_agendamentos"), "consulta", pacienteLogado);
    }
}
