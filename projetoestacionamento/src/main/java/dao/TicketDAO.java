package dao;

import model.Ticket;
import model.Vaga;
import model.Veiculo;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TicketDAO {
    
    private Connection connection;

    public TicketDAO(Connection connection) {
        this.connection = connection;
        criarTabelaSeNaoExistir();
    }

    private void criarTabelaSeNaoExistir() {
        String sql = "CREATE TABLE IF NOT EXISTS tickets (" +
                   "id INT AUTO_INCREMENT PRIMARY KEY, " +
                   "placa VARCHAR(10) NOT NULL, " +
                   "marca VARCHAR(50), " +
                   "modelo VARCHAR(50), " +
                   "cor VARCHAR(20), " +
                   "vaga_id INT NOT NULL, " +
                   "entrada DATETIME NOT NULL, " +
                   "saida DATETIME, " +
                   "valor_cobrado DECIMAL(10,2))";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Erro ao criar tabela tickets: " + e.getMessage());
        }
    }

    public void adicionarTicket(Ticket ticket) {
        String sql = "INSERT INTO tickets (placa, marca, modelo, cor, vaga_id, entrada) " +
                   "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, ticket.getVeiculo().getPlaca());
            pstmt.setString(2, ticket.getVeiculo().getMarca());
            pstmt.setString(3, ticket.getVeiculo().getModelo());
            pstmt.setString(4, ticket.getVeiculo().getCor());
            pstmt.setInt(5, ticket.getVaga().getId());
            pstmt.setTimestamp(6, Timestamp.valueOf(ticket.getEntrada()));
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    ticket.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao adicionar ticket: " + e.getMessage());
        }
    }

    public Ticket buscarTicketPorPlaca(String placa) {
        String sql = "SELECT * FROM tickets WHERE placa = ? AND saida IS NULL ORDER BY entrada DESC LIMIT 1";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, placa);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Veiculo veiculo = new Veiculo(
                        rs.getString("placa"),
                        rs.getString("marca"),
                        rs.getString("modelo"),
                        rs.getString("cor")
                    );
                    
                    Vaga vaga = new Vaga(rs.getInt("vaga_id"));
                    Ticket ticket = new Ticket(veiculo, vaga, rs.getTimestamp("entrada").toLocalDateTime());
                    ticket.setId(rs.getInt("id"));
                    
                    if (rs.getTimestamp("saida") != null) {
                        ticket.setSaida(rs.getTimestamp("saida").toLocalDateTime());
                    }
                    
                    ticket.setValorCobrado(rs.getDouble("valor_cobrado"));
                    return ticket;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar ticket por placa: " + e.getMessage());
        }
        return null;
    }

    public List<Ticket> listarTickets() {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT * FROM tickets ORDER BY entrada DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Veiculo veiculo = new Veiculo(
                    rs.getString("placa"),
                    rs.getString("marca"),
                    rs.getString("modelo"),
                    rs.getString("cor")
                );
                
                Vaga vaga = new Vaga(rs.getInt("vaga_id"));
                
                Ticket ticket = new Ticket(
                    veiculo,
                    vaga,
                    rs.getTimestamp("entrada").toLocalDateTime()
                );
                
                ticket.setId(rs.getInt("id"));
                
                if (rs.getTimestamp("saida") != null) {
                    ticket.setSaida(rs.getTimestamp("saida").toLocalDateTime());
                }
                
                ticket.setValorCobrado(rs.getDouble("valor_cobrado"));
                
                tickets.add(ticket);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar tickets: " + e.getMessage());
        }
        return tickets;
    }
    
    public void registrarSaida(Ticket ticket) {
        String sql = "UPDATE tickets SET saida = ?, valor_cobrado = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(ticket.getSaida()));
            pstmt.setDouble(2, ticket.getValorCobrado());
            pstmt.setInt(3, ticket.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao registrar saída: " + e.getMessage());
        }
    }
}