package com.example.clinicamedica;

import java.time.LocalDate;
import java.time.LocalTime;

public class Agendamento {
    private UsuarioPaciente paciente;
    private LocalDate data;
    private LocalTime hora;

    public Agendamento(UsuarioPaciente paciente, LocalDate data, LocalTime hora) {
        this.paciente = paciente;
        this.data = data;
        this.hora = hora;
    }

    public UsuarioPaciente getPaciente() {
        return paciente;
    }

    public LocalDate getData() {
        return data;
    }

    public LocalTime getHora() {
        return hora;
    }
}
