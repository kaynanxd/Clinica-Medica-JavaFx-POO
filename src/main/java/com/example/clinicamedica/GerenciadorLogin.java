package com.example.clinicamedica;



public class GerenciadorLogin {

    private static GerenciadorLogin instancia;

    public static synchronized GerenciadorLogin getInstancia() {
        if (instancia == null) {
            instancia = new GerenciadorLogin();
        }
        return instancia;
    }



    public Usuario verificarCredenciais(String identificador, String senha) {
        Usuario usuarioEncontrado = null;
        //  Tenta buscar por ID
        try {
            int idNumerico = Integer.parseInt(identificador);

            usuarioEncontrado = LerArquivo.buscarUsuarioPorId(idNumerico, "Medico");
            if (usuarioEncontrado != null) {
                if (usuarioEncontrado.getSenha().equals(senha)) {
                    return usuarioEncontrado; // ID e senha batem
                } else {
                    return null;
                }
            }

            usuarioEncontrado = LerArquivo.buscarUsuarioPorId(idNumerico, "Paciente");
            if (usuarioEncontrado != null) {
                if (usuarioEncontrado.getSenha().equals(senha)) {
                    return usuarioEncontrado;
                } else {
                    return null;
                }
            }

        } catch (NumberFormatException e) {
            // agora busca pelo nome
        }

        // Tenta buscar por Nome
        usuarioEncontrado = LerArquivo.buscarMedicoPorNome(identificador);
        if (usuarioEncontrado != null) {
            if (usuarioEncontrado.getSenha().equals(senha)) {
                return usuarioEncontrado;
            } else {
                return null;
            }
        }

        usuarioEncontrado = LerArquivo.buscarPacientePorNome(identificador);
        if (usuarioEncontrado != null) {
            if (usuarioEncontrado.getSenha().equals(senha)) {
                return usuarioEncontrado;
            } else {
                return null;
            }
        }
        return null;
    }
}