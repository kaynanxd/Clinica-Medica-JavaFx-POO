package com.example.clinicamedica;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class RealizarConsulta {
    private UsuarioMedico medico;
    private static Map<String, Boleto> boletosPendentes = new HashMap<>();

    public RealizarConsulta(UsuarioMedico medico) {
        this.medico = medico;
    }

    public static Boleto gerarBoletoComEspecialidadePreco(UsuarioMedico medico, UsuarioPaciente paciente) {

        double valor = ConfiguracaoValores.getValor(medico.getEspecialidade());
        Boleto novoBoleto = new Boleto(valor, paciente.getId(), medico.getNome(), medico.getEspecialidade());
        boletosPendentes.put(novoBoleto.getCodigo(), novoBoleto);

        return novoBoleto;
    }


    public void realizarConsulta(String dataInput, String horarioConsulta, String idPaciente,
                                 String sintomas, String tratamentoSugerido, String exames, String medicamentos) {
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
            idPacienteInt = Integer.parseInt(idPaciente); //converte string pra int
        } catch (NumberFormatException e) {
            System.out.println("ID do paciente inválido.");
            return;
        }

        UsuarioPaciente paciente = (UsuarioPaciente) LerArquivo.buscarUsuarioPorId(idPacienteInt, "Paciente"); //procura paciente poor id
        if (paciente == null) {
            System.out.println("Paciente não encontrado.");
            return;
        }

        LocalDateTime dataHoraConsulta = LocalDateTime.of(data, hora);

        Consulta consulta = new Consulta(medico, paciente, dataHoraConsulta, StatusConsulta.AGENDADA);
        consulta.atualizarStatusNaConsulta(StatusConsulta.REALIZADA);

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

        if (paciente.getPlanoSaude().equalsIgnoreCase("Sem")) {
            Boleto boleto = gerarBoletoComEspecialidadePreco(medico, paciente); //verifica a especialidade pra definir o preco
            String conteudoBoleto = boleto.gerarConteudoBoleto(); //cria o boleto em txt

            String nomeArquivoBoleto = "boleto_" + boleto.getCodigo() + ".txt";
            String pastaBoletos = "dados_boletos/" + paciente.getId() + "/";
            new File(pastaBoletos).mkdirs();

            EscreverArquivo.escreverEmArquivo(nomeArquivoBoleto, pastaBoletos, conteudoBoleto);
            System.out.println("Boleto gerado: " + boleto.getCodigo());
        }
    }
      public void visualizarConsultas () {
            System.out.print("\n=== Visualizar Consultas ===\n");
            System.out.print("\n --- Consultas Marcadas ---\n");
            String caminho = "dados_agendamentos/" + medico.getId() + "/";
            File pasta = new File(caminho);
            LerArquivo.listarArquivos(pasta, "consulta", medico);
        }
    }
