package dao;

import model.Vaga;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VagaDAO {
    private final Connection connection;

    public VagaDAO(Connection connection) {
        this.connection = connection;
        criarTabelaSeNaoExistir();
        verificarEInicializarVagas();
    }

    private void criarTabelaSeNaoExistir() {
        String sql = "CREATE TABLE IF NOT EXISTS vagas (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "numero INT NOT NULL UNIQUE, " +
                     "ocupada BOOLEAN NOT NULL DEFAULT FALSE, " +
                     "data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Erro ao criar tabela vagas: " + e.getMessage());
            throw new RuntimeException("Falha ao criar tabela de vagas", e);
        }
    }

    private void verificarEInicializarVagas() {
        try {
            if (!tabelaContemDados()) {
                inicializarVagas(20); // Valor padrão de 20 vagas
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar vagas: " + e.getMessage());
        }
    }

    private boolean tabelaContemDados() throws SQLException {
        String sql = "SELECT COUNT(*) FROM vagas";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public synchronized void inicializarVagas(int quantidadeVagas) {
        if (quantidadeVagas <= 0) {
            throw new IllegalArgumentException("Quantidade de vagas deve ser positiva");
        }

        try {
            connection.setAutoCommit(false);
            
            // Limpa a tabela completamente
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
                stmt.execute("TRUNCATE TABLE vagas");
                stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            }

            // Insere novas vagas usando batch
            String sql = "INSERT INTO vagas (numero, ocupada) VALUES (?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                for (int i = 1; i <= quantidadeVagas; i++) {
                    pstmt.setInt(1, i);
                    pstmt.setBoolean(2, false);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            connection.commit();
            System.out.println(quantidadeVagas + " vagas inicializadas com sucesso");
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.err.println("Erro ao fazer rollback: " + ex.getMessage());
            }
            System.err.println("Erro ao inicializar vagas: " + e.getMessage());
            throw new RuntimeException("Falha na inicialização das vagas", e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Erro ao reativar auto-commit: " + e.getMessage());
            }
        }
    }

    public Vaga buscarVagaLivre() {
        String sql = "SELECT id, numero, ocupada FROM vagas WHERE ocupada = false ORDER BY numero LIMIT 1 FOR UPDATE";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, 
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return new Vaga(
                    rs.getInt("id"),
                    rs.getInt("numero"),
                    rs.getBoolean("ocupada")
                );
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Erro ao buscar vaga livre: " + e.getMessage());
            throw new RuntimeException("Falha ao buscar vaga livre", e);
        }
    }

    public synchronized boolean ocuparVaga(Vaga vaga) {
        String sql = "UPDATE vagas SET ocupada = true WHERE id = ? AND ocupada = false";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, vaga.getId());
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                vaga.ocupar();
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Erro ao ocupar vaga: " + e.getMessage());
            throw new RuntimeException("Falha ao ocupar vaga", e);
        }
    }

    public synchronized boolean liberarVaga(Vaga vaga) {
        String sql = "UPDATE vagas SET ocupada = false WHERE id = ? AND ocupada = true";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, vaga.getId());
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                vaga.liberar();
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Erro ao liberar vaga: " + e.getMessage());
            throw new RuntimeException("Falha ao liberar vaga", e);
        }
    }

    public List<Vaga> listarVagas() {
        List<Vaga> vagas = new ArrayList<>();
        String sql = "SELECT id, numero, ocupada FROM vagas ORDER BY numero";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                vagas.add(new Vaga(
                    rs.getInt("id"),
                    rs.getInt("numero"),
                    rs.getBoolean("ocupada")
                ));
            }
            return vagas;
        } catch (SQLException e) {
            System.err.println("Erro ao listar vagas: " + e.getMessage());
            throw new RuntimeException("Falha ao listar vagas", e);
        }
    }

    public Vaga buscarPorId(int id) {
        String sql = "SELECT id, numero, ocupada FROM vagas WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Vaga(
                        rs.getInt("id"),
                        rs.getInt("numero"),
                        rs.getBoolean("ocupada")
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar vaga por ID: " + e.getMessage());
            throw new RuntimeException("Falha ao buscar vaga por ID", e);
        }
    }

    public int contarVagasLivres() {
        String sql = "SELECT COUNT(*) FROM vagas WHERE ocupada = false";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            System.err.println("Erro ao contar vagas livres: " + e.getMessage());
            throw new RuntimeException("Falha ao contar vagas livres", e);
        }
    }
}