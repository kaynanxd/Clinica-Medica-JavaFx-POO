import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsuarioMedico extends Usuario {
    private String especialidade;
    private List<String> planosSaudeAtendidos;
    private List<Agendamento> agendamentos;

    public UsuarioMedico(String senha, String nome, String especialidade, List<String> planosSaudeAtendidos) {
        super(senha, nome, "Medico");
        this.especialidade = especialidade;
        this.planosSaudeAtendidos = new ArrayList<>(planosSaudeAtendidos);
        this.agendamentos = new ArrayList<>();
    }

    // Retorna todos os agendamentos
    public List<Agendamento> getAgendamentos() {
        return new ArrayList<>(agendamentos);
    }

    // Adiciona um agendamento
    public void adicionarAgendamento(Agendamento agendamento) {
        agendamentos.add(agendamento);
    }

    // Método para agrupar agendamentos por data
    public Map<LocalDate, List<Agendamento>> getAgendamentosPorData() {
        Map<LocalDate, List<Agendamento>> mapa = new HashMap<>();
        for (Agendamento ag : agendamentos) {
            mapa.computeIfAbsent(ag.getData(), k -> new ArrayList<>()).add(ag);
        }
        return mapa;
    }

    // Getters e setters simples
    public String getEspecialidade() {
        return especialidade;
    }

    public List<String> getPlanosSaudeAtendidos() {
        return new ArrayList<>(planosSaudeAtendidos);
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public void setPlanosSaudeAtendidos(List<String> planosSaudeAtendidos) {
        this.planosSaudeAtendidos = new ArrayList<>(planosSaudeAtendidos);
    }
}
