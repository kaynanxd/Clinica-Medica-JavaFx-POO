import java.util.ArrayList;
import java.util.List;

public class UsuarioMedico extends Usuario {
    private String especialidade;
    private List<String> planosSaudeAtendidos;
    /* // Atributos que podem ser necessarios para criar a parte d consulta e avaliacao
    private List<Avaliacao> avaliacoes; // Para armazenar as avaliações recebidas
    private double mediaEstrelas; // Para armazenar a média das estrelas
    private Map<LocalDate, List<Agendamento>> agendamentosPorData; // Estrutura para gerenciar agendamentos por data
    private Map<LocalDate, List<UsuarioPaciente>> listaEsperaPorData; // Estrutura para gerenciar lista de espera por data
    */
    public UsuarioMedico(String senha, String nome, String especialidade, List<String> planosSaudeAtendidos) {
        super(senha, nome, "Medico");
        this.especialidade = especialidade;
        this.planosSaudeAtendidos = new ArrayList<>(planosSaudeAtendidos);
    }

    /* metodos que podem ser necessarios para implementar avaliacao e consulta
    public void adicionarAvaliacao(Avaliacao avaliacao) {
        this.avaliacoes.add(avaliacao);
        // Recalcular a média de estrelas
        double somaEstrelas = 0;
        for (Avaliacao av : avaliacoes) {
            somaEstrelas += av.getEstrelas();
        }
        this.mediaEstrelas = avaliacoes.isEmpty() ? 0 : somaEstrelas / avaliacoes.size();
    }

    // --- Métodos Adicionais para Agendamentos e Lista de Espera ---
    public List<Agendamento> getAgendamentosDoDia(LocalDate data) {
        return agendamentosPorData.getOrDefault(data, new ArrayList<>());
    }

    public void adicionarAgendamento(LocalDate data, Agendamento agendamento) {
        agendamentosPorData.computeIfAbsent(data, k -> new ArrayList<>()).add(agendamento);
    }

    public List<UsuarioPaciente> getListaEsperaDoDia(LocalDate data) {
        return listaEsperaPorData.getOrDefault(data, new ArrayList<>());
    }

    public void adicionarPacienteNaListaEspera(LocalDate data, UsuarioPaciente paciente) {
        listaEsperaPorData.computeIfAbsent(data, k -> new ArrayList<>()).add(paciente);
    }

    public void removerPacienteDaListaEspera(LocalDate data, UsuarioPaciente paciente) {
        List<UsuarioPaciente> lista = listaEsperaPorData.get(data);
        if (lista != null) {
            lista.remove(paciente);
            if (lista.isEmpty()) {
                listaEsperaPorData.remove(data); // Remove a entrada se a lista ficar vazia
            }
        }
    }
    */
    // --- Getters ---
    public String getEspecialidade() {return especialidade;}
    public List<String> getPlanosSaudeAtendidos() {return new ArrayList<>(planosSaudeAtendidos); }
    /* getters que podem ser necessarios
    public Map<LocalDate, List<Agendamento>> getTodosAgendamentos() { return new HashMap<>(agendamentosPorData); }
    public Map<LocalDate, List<UsuarioPaciente>> getTodasListasEspera() { return new HashMap<>(listaEsperaPorData); }
    */
    // --- Setters ---
    public void setEspecialidade(String especialidade) {this.especialidade = especialidade;}
    public void setPlanosSaudeAtendidos(List<String> planosSaudeAtendidos) {this.planosSaudeAtendidos = new ArrayList<>(planosSaudeAtendidos);}
}