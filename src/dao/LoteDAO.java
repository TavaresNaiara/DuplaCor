package dao;

import model.Lote;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operações CRUD da tabela Lote (com suporte ao algoritmo FEFO - First Expired, First Out).
 */
public class LoteDAO {

    public boolean inserir(Lote lote) {
        String sql = "INSERT INTO Lote (quantInicial, quantAtual, dataValidade, dataEntrada, status, Produto_idProduto) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, lote.getQuantInicial());
            stmt.setInt(2, lote.getQuantAtual());
            stmt.setDate(3, Date.valueOf(lote.getDataValidade()));
            stmt.setDate(4, Date.valueOf(lote.getDataEntrada()));
            stmt.setString(5, lote.getStatus() != null ? lote.getStatus() : "DISPONIVEL");
            stmt.setInt(6, lote.getProdutoId());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        lote.setIdLote(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inserir Lote: " + e.getMessage());
        }
        return false;
    }

    public Lote buscarPorId(int id) {
        String sql = "SELECT idLote, quantInicial, quantAtual, dataValidade, dataEntrada, status, Produto_idProduto " +
                     "FROM Lote WHERE idLote = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar Lote por ID: " + e.getMessage());
        }
        return null;
    }

    public List<Lote> listarTodos() {
        List<Lote> lista = new ArrayList<>();
        String sql = "SELECT idLote, quantInicial, quantAtual, dataValidade, dataEntrada, status, Produto_idProduto " +
                     "FROM Lote ORDER BY idLote ASC";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar Lotes: " + e.getMessage());
        }
        return lista;
    }

    public List<Lote> listarPorProduto(int produtoId) {
        List<Lote> lista = new ArrayList<>();
        String sql = "SELECT idLote, quantInicial, quantAtual, dataValidade, dataEntrada, status, Produto_idProduto " +
                     "FROM Lote WHERE Produto_idProduto = ? ORDER BY dataValidade ASC";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, produtoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar Lotes por Produto: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Algoritmo FEFO (First Expired, First Out):
     * Retorna lotes DISPONÍVEIS, com saldo > 0, dentro do prazo de validade,
     * ordenados pela data de validade mais próxima (menor data primeiro).
     */
    public List<Lote> listarDisponiveisFEFO(int produtoId) {
        List<Lote> lista = new ArrayList<>();
        String sql = "SELECT idLote, quantInicial, quantAtual, dataValidade, dataEntrada, status, Produto_idProduto " +
                     "FROM Lote WHERE Produto_idProduto = ? AND status = 'DISPONIVEL' " +
                     "AND quantAtual > 0 AND dataValidade >= CURDATE() " +
                     "ORDER BY dataValidade ASC";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, produtoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar Lotes FEFO: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Retorna todos os lotes que estão com data de validade expirada (para relatório de perdas / auditoria).
     */
    public List<Lote> listarVencidos() {
        List<Lote> lista = new ArrayList<>();
        String sql = "SELECT idLote, quantInicial, quantAtual, dataValidade, dataEntrada, status, Produto_idProduto " +
                     "FROM Lote WHERE dataValidade < CURDATE() ORDER BY dataValidade ASC";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar Lotes Vencidos: " + e.getMessage());
        }
        return lista;
    }

    public boolean atualizar(Lote lote) {
        String sql = "UPDATE Lote SET quantInicial = ?, quantAtual = ?, dataValidade = ?, dataEntrada = ?, " +
                     "status = ?, Produto_idProduto = ? WHERE idLote = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, lote.getQuantInicial());
            stmt.setInt(2, lote.getQuantAtual());
            stmt.setDate(3, Date.valueOf(lote.getDataValidade()));
            stmt.setDate(4, Date.valueOf(lote.getDataEntrada()));
            stmt.setString(5, lote.getStatus());
            stmt.setInt(6, lote.getProdutoId());
            stmt.setInt(7, lote.getIdLote());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar Lote: " + e.getMessage());
        }
        return false;
    }

    public boolean atualizarQuantidade(int idLote, int novaQtd) {
        String sql = "UPDATE Lote SET quantAtual = ?, status = CASE WHEN ? <= 0 THEN 'ESGOTADO' ELSE status END WHERE idLote = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, novaQtd);
            stmt.setInt(2, novaQtd);
            stmt.setInt(3, idLote);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar quantidade do Lote: " + e.getMessage());
        }
        return false;
    }

    public boolean atualizarStatus(int idLote, String novoStatus) {
        String sql = "UPDATE Lote SET status = ? WHERE idLote = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, novoStatus);
            stmt.setInt(2, idLote);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar status do Lote: " + e.getMessage());
        }
        return false;
    }

    public boolean excluir(int id) {
        String sql = "DELETE FROM Lote WHERE idLote = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir Lote: " + e.getMessage());
        }
        return false;
    }

    private Lote mapearResultSet(ResultSet rs) throws SQLException {
        Date dtVal = rs.getDate("dataValidade");
        Date dtEnt = rs.getDate("dataEntrada");
        return new Lote(
                rs.getInt("idLote"),
                rs.getInt("quantInicial"),
                rs.getInt("quantAtual"),
                dtVal != null ? dtVal.toLocalDate() : null,
                dtEnt != null ? dtEnt.toLocalDate() : null,
                rs.getString("status"),
                rs.getInt("Produto_idProduto")
        );
    }
}
