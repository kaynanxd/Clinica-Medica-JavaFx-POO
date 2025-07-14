package com.example.clinicamedica;

public class UsuarioPaciente extends Usuario {
    private int idade;
    private String planoSaude;

    public UsuarioPaciente(String senha, String nome, int idade, String planoSaude) {
        super(senha, nome, "Paciente");
        this.idade = idade;
        this.planoSaude = planoSaude;
    }

    //gets
    public int getIdade() {return idade;}
    public String getPlanoSaude() {return planoSaude;}

    //sets
    public void setIdade(int idade) {this.idade = idade;}
    public void setPlanoSaude(String planoSaude) {this.planoSaude = planoSaude;}
}