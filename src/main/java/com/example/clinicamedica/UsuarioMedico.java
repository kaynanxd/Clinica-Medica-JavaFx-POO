package com.example.clinicamedica;

import java.util.ArrayList;
import java.util.List;

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

    public List<Agendamento> getAgendamentos() {
        return new ArrayList<>(agendamentos);
    }

    public void adicionarAgendamento(Agendamento agendamento) {
        agendamentos.add(agendamento);
    }

    //gets
    public String getEspecialidade() {
        return especialidade;
    }
    public List<String> getPlanosSaudeAtendidos() {
        return new ArrayList<>(planosSaudeAtendidos);
    }
    //sets
    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }
    public void setPlanosSaudeAtendidos(List<String> planosSaudeAtendidos) {
        this.planosSaudeAtendidos = new ArrayList<>(planosSaudeAtendidos);
    }
}
