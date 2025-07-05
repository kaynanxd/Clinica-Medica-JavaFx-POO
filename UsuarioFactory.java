import java.util.List;
import java.util.Random;

public class UsuarioFactory {

    private static final Random random = new Random();
    private static final int MAX_ID = 9999;
    private static final int MIN_ID = 1;

    private UsuarioFactory() {
    }

    private static int gerarIdAleatorio() {
        return random.nextInt(MAX_ID - MIN_ID + 1) + MIN_ID;
    }
    public static UsuarioMedico criarMedico(String senha, String nome, String especialidade, List<String> planosSaudeAtendidos) {
        int idGerado = gerarIdAleatorio();
        UsuarioMedico medico = new UsuarioMedico(senha, nome, especialidade, planosSaudeAtendidos);
        medico.setId(idGerado);
        return medico;
    }
    public static UsuarioPaciente criarPaciente(String senha, String nome, int idade, String planoSaude) {
        int idGerado = gerarIdAleatorio();
        UsuarioPaciente paciente = new UsuarioPaciente(senha, nome, idade, planoSaude);
        paciente.setId(idGerado);
        return paciente;
    }
}