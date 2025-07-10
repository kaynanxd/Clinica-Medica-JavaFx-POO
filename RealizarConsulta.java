import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class RealizarConsulta {
    private UsuarioMedico medico;

    public RealizarConsulta(UsuarioMedico medico) {
        this.medico = medico;
    }

    public void realizarConsulta(String dataInput, String horarioConsulta, String idPaciente,
                                 String sintomas, String tratamentoSugerido,
                                 String exames, String medicamentos) {
        System.out.print("\n=== Realizar Consulta ===\n");
        System.out.print("\n --- Consultas Marcadas ---\n");

        String caminho = "dados_agendamentos/" + medico.getId() + "/";
        File pasta = new File(caminho);
        LerArquivo.listarArquivos(pasta, "consulta", medico);

        // Formatar data para nome de arquivo
        DateTimeFormatter formatoEntrada = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatoArquivo = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate data = LocalDate.parse(dataInput, formatoEntrada);
        String dataConsulta = data.format(formatoArquivo);

        LocalTime hora = LocalTime.parse(horarioConsulta);

        int idPacienteInt;
        try {
            idPacienteInt = Integer.parseInt(idPaciente);
        } catch (NumberFormatException e) {
            System.out.println("ID do paciente inválido.");
            return;
        }

        UsuarioPaciente paciente = (UsuarioPaciente) LerArquivo.buscarUsuarioPorId(idPacienteInt, "Paciente");
        if (paciente == null) {
            System.out.println("Paciente não encontrado.");
            return;
        }

        LocalDateTime dataHoraConsulta = LocalDateTime.of(data, hora);
        Consulta consulta = new Consulta(medico, paciente, dataHoraConsulta, StatusConsulta.AGENDADA);
        consulta.atualizarStatusNoArquivo(StatusConsulta.REALIZADA);

        String conteudo = "Paciente ID: " + idPaciente +
                "\nData: " + dataConsulta +
                "\nHorário: " + horarioConsulta +
                "\nSintomas: " + sintomas +
                "\nTratamento sugerido: " + tratamentoSugerido +
                "\nExames: " + exames +
                "\nMedicamentos: " + medicamentos +
                "\nStatus: REALIZADA";

        String nomeArquivoRelatorio = idPaciente + "_" + dataConsulta + "_" + horarioConsulta.replace(":", "-") + "_relatorio.txt";
        String pastaRelatorio = "dados_consultas/" + medico.getId() + "/";
        new File(pastaRelatorio).mkdirs();

        EscreverArquivo.escreverEmArquivo(nomeArquivoRelatorio, pastaRelatorio, conteudo);
        System.out.println("Consulta registrada com sucesso.");
    }

    public void visualizarConsultas() {
        System.out.print("\n=== Visualizar Consultas ===\n");
        System.out.print("\n --- Consultas Marcadas ---\n");
        String caminho = "dados_agendamentos/" + medico.getId() + "/";
        File pasta = new File(caminho);
        LerArquivo.listarArquivos(pasta, "consulta", medico);
    }
}
