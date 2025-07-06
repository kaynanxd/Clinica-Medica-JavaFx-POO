
public class GerenciadorLogin {

    public Usuario verificarCredenciais(String identificador, String senha) {
        Usuario usuarioEncontrado = null;
        //  Tenta buscar por ID
        try {
            int idNumerico = Integer.parseInt(identificador);

            // Tenta buscar como Médico pelo ID
            usuarioEncontrado = LerArquivo.buscarUsuarioPorId(idNumerico, "Medico");
            if (usuarioEncontrado != null) {
                if (usuarioEncontrado.getSenha().equals(senha)) {
                    return usuarioEncontrado; // ID e senha batem
                } else {
                    return null;
                }
            }
            // Se não encontrou médico tenta buscar Paciente
            usuarioEncontrado = LerArquivo.buscarUsuarioPorId(idNumerico, "Paciente");
            if (usuarioEncontrado != null) {
                if (usuarioEncontrado.getSenha().equals(senha)) {
                    return usuarioEncontrado; // ID de paciente e senha batem
                } else {
                    return null;
                }
            }

        } catch (NumberFormatException e) {
            // a execução continua para a busca por nome.
        }

        // Tenta buscar como Médico por Nome
        usuarioEncontrado = LerArquivo.buscarMedicoPorNome(identificador);
        if (usuarioEncontrado != null) {
            if (usuarioEncontrado.getSenha().equals(senha)) {
                return usuarioEncontrado; // Nome e senha batem
            } else {
                return null;
            }
        }

        // Tenta buscar como Paciente por Nome
        usuarioEncontrado = LerArquivo.buscarPacientePorNome(identificador);
        if (usuarioEncontrado != null) {
            if (usuarioEncontrado.getSenha().equals(senha)) {
                return usuarioEncontrado; // Nome e senha batem
            } else {
                return null;
            }
        }
        // Se chegou até aqui, o usuário não foi encontrado
        return null;
    }
}