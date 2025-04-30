package app;

//conexao ao sql
import db.ConexaoMySQL;
import java.sql.Connection;

//imports entre os arquivos java
import dao.TicketDAO;
import dao.VagaDAO;
import model.Ticket;
import model.Vaga;
import model.Veiculo;
import service.EstacionamentoService;

//funcionalidades do código (relógios e scanners)
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.List; 

public class Principal {

    private static final boolean DEBUG = true;

    public static void main(String[] args) {
        try {
            // Estabelece conexão com o MySQL
            Connection connection = ConexaoMySQL.getConexao();
            
            // Inicializa os DAOs com a conexão
            VagaDAO vagaDAO = new VagaDAO(connection);
            TicketDAO ticketDAO = new TicketDAO(connection);
            
            // Inicializa as vagas no banco de dados (20 vagas)
            vagaDAO.inicializarVagas(20);
            
            EstacionamentoService estacionamentoService = new EstacionamentoService();
            Scanner scanner = new Scanner(System.in);
            boolean executando = true;

            while (executando) {
                System.out.println("\n==== SISTEMA ESTACIONAMENTO ====");
                System.out.println("1. Entrada de Veículo");
                System.out.println("2. Saída de Veículo");
                System.out.println("3. Listar Vagas");
                System.out.println("4. Listar Tickets");
                System.out.println("0. Sair");
                System.out.print("Escolha uma opção: ");

                int opcao = scanner.nextInt();
                scanner.nextLine(); // quebra de linha

                switch (opcao) {
                    case 1:
                        entradaVeiculo(scanner, vagaDAO, ticketDAO);
                        break;

                    case 2:
                        System.out.print("Placa do veículo para saída: ");
                        String placaSaida = scanner.nextLine();

                        if (DEBUG) System.out.println("Buscando ticket para placa: " + placaSaida);

                        Ticket ticketSaida = ticketDAO.buscarTicketPorPlaca(placaSaida);

                        if (ticketSaida == null) {
                            System.out.println("Nenhum ticket ativo encontrado para esta placa.");
                        } else {
                            if (DEBUG) System.out.println("Ticket encontrado: " + ticketSaida.getId());

                            ticketSaida.setSaida(LocalDateTime.now());
                            estacionamentoService.calcularValor(ticketSaida);

                            if (DEBUG) System.out.println("Valor calculado: " + ticketSaida.getValorCobrado());

                            ticketDAO.registrarSaida(ticketSaida);
                            vagaDAO.liberarVaga(ticketSaida.getVaga());

                            System.out.println("\n--- COMPROVANTE DE SAÍDA ---");
                            System.out.printf("Placa: %s%n", ticketSaida.getVeiculo().getPlaca());
                            System.out.printf("Vaga: %d%n", ticketSaida.getVaga().getNumero());
                            System.out.printf("Entrada: %s%n", ticketSaida.getEntrada());
                            System.out.printf("Saída: %s%n", ticketSaida.getSaida());
                            System.out.printf("Tempo estacionado: %d minutos%n",
                                java.time.Duration.between(ticketSaida.getEntrada(), ticketSaida.getSaida()).toMinutes());
                            System.out.printf("Valor a pagar: R$%.2f%n", ticketSaida.getValorCobrado());
                        }
                        break;

                    case 3:
                        System.out.println("\n--- LISTA DE VAGAS ---");
                        List<Vaga> vagas = vagaDAO.listarVagas();
                        if (vagas.isEmpty()) {
                            System.out.println("Nenhuma vaga cadastrada!");
                        } else {
                            for (Vaga v : vagas) {
                                System.out.printf("Vaga %d: %s%n", 
                                    v.getNumero(), 
                                    v.isOcupada() ? "OCUPADA" : "LIVRE");
                            }
                        }
                        break;

                    case 4:
                        System.out.println("\n--- LISTA DE TICKETS ---");
                        List<Ticket> tickets = ticketDAO.listarTickets();
                        if (tickets.isEmpty()) {
                            System.out.println("Nenhum ticket encontrado!");
                        } else {
                            for (Ticket t : tickets) {
                                System.out.printf(
                                    "Ticket ID: %d | Placa: %s | Vaga: %d | Entrada: %s | Saída: %s | Valor: R$%.2f%n",
                                    t.getId(),
                                    t.getVeiculo().getPlaca(),
                                    t.getVaga().getNumero(),
                                    t.getEntrada(),
                                    t.getSaida() != null ? t.getSaida() : "Em aberto",
                                    t.getValorCobrado()
                                );
                            }
                        }
                        break;

                    case 0:
                        executando = false;
                        System.out.println("Saindo do sistema...");
                        break;

                    default:
                        System.out.println("Opção inválida!");
                }
            }
            
            scanner.close();
            connection.close();
        } catch (Exception e) {
            System.err.println("Erro no sistema: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void entradaVeiculo(Scanner scanner, VagaDAO vagaDAO, TicketDAO ticketDAO) {
        System.out.print("Placa do veículo: ");
        String placa = scanner.nextLine();
        System.out.print("Marca do veículo: ");
        String marca = scanner.nextLine();
        System.out.print("Modelo do veículo: ");
        String modelo = scanner.nextLine();
        System.out.print("Cor do veículo: ");
        String cor = scanner.nextLine();

        Vaga vaga = vagaDAO.buscarVagaLivre();
        if (vaga != null) {
            Veiculo veiculo = new Veiculo(placa, marca, modelo, cor);
            Ticket ticket = new Ticket(veiculo, vaga, LocalDateTime.now());
            vagaDAO.ocuparVaga(vaga);
            ticketDAO.adicionarTicket(ticket);
            System.out.println("Veículo estacionado na vaga " + vaga.getNumero());
        } else {
            System.out.println("Estacionamento cheio!");
        }
    }
    
    private static void saidaVeiculo(Scanner scanner, VagaDAO vagaDAO, TicketDAO ticketDAO, 
                                    EstacionamentoService estacionamentoService) {
        System.out.print("Placa do veículo: ");
        String placaSaida = scanner.nextLine();
        Ticket ticketSaida = ticketDAO.buscarTicketPorPlaca(placaSaida);

        if (ticketSaida != null && ticketSaida.getSaida() == null) {
            ticketSaida.setSaida(LocalDateTime.now());
            estacionamentoService.calcularValor(ticketSaida);
            ticketDAO.registrarSaida(ticketSaida);
            vagaDAO.liberarVaga(ticketSaida.getVaga());

            System.out.println("Saída registrada.");
            System.out.println("Tempo estacionado: " + 
                java.time.Duration.between(ticketSaida.getEntrada(), ticketSaida.getSaida()).toMinutes() + " minutos");
            System.out.println("Valor a pagar: R$" + ticketSaida.getValorCobrado());
        } else if (ticketSaida != null && ticketSaida.getSaida() != null) {
            System.out.println("Este veículo já teve a saída registrada.");
        } else {
            System.out.println("Ticket não encontrado para essa placa.");
        }
    }
    
    private static void listarVagas(VagaDAO vagaDAO) {
        for (Vaga v : vagaDAO.listarVagas()) {
            System.out.println("Vaga " + v.getNumero() + ": " + (v.isOcupada() ? "OCUPADA" : "LIVRE"));
        }
    }
    
    private static void listarTickets(TicketDAO ticketDAO) {
        for (Ticket t : ticketDAO.listarTickets()) {
            System.out.println("Placa: " + t.getVeiculo().getPlaca() + 
                             ", Marca: " + t.getVeiculo().getMarca() +
                             ", Modelo: " + t.getVeiculo().getModelo() +
                             ", Cor: " + t.getVeiculo().getCor() +
                             ", Entrada: " + t.getEntrada() + 
                             ", Saída: " + (t.getSaida() != null ? t.getSaida() : "Em aberto") + 
                             ", Valor: R$" + (t.getValorCobrado() > 0 ? t.getValorCobrado() : "Em aberto"));
        }
    }
}