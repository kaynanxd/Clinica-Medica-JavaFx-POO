import java.io.File;

public class Avaliacao {
    private UsuarioPaciente paciente;

    public Avaliacao(UsuarioPaciente paciente) {
        this.paciente = paciente;
    }

    public void avaliarConsulta(String dataConsulta, String horarioConsulta,
                                String nomeMedico, String textoAvaliacao, double estrelasEscolhidas) {
        System.out.print("\n=== Avaliar Consulta ===\n");

        visualizarConsultas();

        UsuarioMedico medico = LerArquivo.buscarMedicoPorNome(nomeMedico);
        if (medico == null) {
            System.out.println("Médico não encontrado.");
            return;
        }

        String conteudo = "Usuário: " + paciente.getNome() +
                "\nTexto: " + textoAvaliacao +
                "\nNotas: " + getEstrelasString(estrelasEscolhidas);

        String dataFormatada = dataConsulta.replace("/", "-");
        String horarioFormatado = horarioConsulta.replace(":", "-");
        String nomeArquivoRelatorio = medico.getId() + "_" + dataFormatada + "_" + horarioFormatado + "_avaliacao.txt";

        String pastaRelatorio = "dados_avaliacoes/" + medico.getId() + "/";
        new File(pastaRelatorio).mkdirs();
        EscreverArquivo.escreverEmArquivo(nomeArquivoRelatorio, pastaRelatorio, conteudo);
        System.out.println("Avaliação registrada com sucesso.");
    }

    public void visualizarConsultas() {
        String caminho = "dados_agendamentos/";
        File pasta = new File(caminho);
        LerArquivo.listarArquivos(pasta, "consulta", paciente);
    }

    public static String getEstrelasString(double nota) {
        int estrelaCheia = (int) nota;
        boolean meiaEstrela = (nota - estrelaCheia) >= 0.5;
        StringBuilder estrelas = new StringBuilder();

        for (int i = 0; i < estrelaCheia; i++)
            estrelas.append("⭐");
        if (meiaEstrela)
            estrelas.append("½");

        return estrelas.toString();
    }
}
