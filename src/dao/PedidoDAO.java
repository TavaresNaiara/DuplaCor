package dao;

import model.Pedido;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operações CRUD da tabela Pedido.
 */
public class PedidoDAO {

    public boolean inserir(Pedido pedido) {
        String sql = "INSERT INTO Pedido (dataVenda, total, statusPagamento, Usuario_idUsuario) VALUES (?, ?, ?, ?)";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (pedido.getDataVenda() != null) {
                stmt.setTimestamp(1, Timestamp.valueOf(pedido.getDataVenda()));
            } else {
                stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            }

            stmt.setBigDecimal(2, pedido.getTotal());
            stmt.setString(3, pedido.getStatusPagamento() != null ? pedido.getStatusPagamento() : "APROVADO");
            stmt.setInt(4, pedido.getUsuarioId());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        pedido.setIdPedido(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inserir Pedido: " + e.getMessage());
        }
        return false;
    }

    public Pedido buscarPorId(int id) {
        String sql = "SELECT idPedido, dataVenda, total, statusPagamento, Usuario_idUsuario FROM Pedido WHERE idPedido = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar Pedido por ID: " + e.getMessage());
        }
        return null;
    }

    public List<Pedido> listarTodos() {
        List<Pedido> lista = new ArrayList<>();
        String sql = "SELECT idPedido, dataVenda, total, statusPagamento, Usuario_idUsuario FROM Pedido ORDER BY dataVenda DESC";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar Pedidos: " + e.getMessage());
        }
        return lista;
    }

    public List<Pedido> listarPorUsuario(int usuarioId) {
        List<Pedido> lista = new ArrayList<>();
        String sql = "SELECT idPedido, dataVenda, total, statusPagamento, Usuario_idUsuario FROM Pedido WHERE Usuario_idUsuario = ? ORDER BY dataVenda DESC";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar Pedidos por Usuário: " + e.getMessage());
        }
        return lista;
    }

    public boolean atualizar(Pedido pedido) {
        String sql = "UPDATE Pedido SET total = ?, statusPagamento = ?, Usuario_idUsuario = ? WHERE idPedido = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, pedido.getTotal());
            stmt.setString(2, pedido.getStatusPagamento());
            stmt.setInt(3, pedido.getUsuarioId());
            stmt.setInt(4, pedido.getIdPedido());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar Pedido: " + e.getMessage());
        }
        return false;
    }

    public boolean atualizarStatusPagamento(int idPedido, String novoStatus) {
        String sql = "UPDATE Pedido SET statusPagamento = ? WHERE idPedido = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, novoStatus);
            stmt.setInt(2, idPedido);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar status do Pedido: " + e.getMessage());
        }
        return false;
    }

    public boolean excluir(int id) {
        String sql = "DELETE FROM Pedido WHERE idPedido = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir Pedido: " + e.getMessage());
        }
        return false;
    }

    private Pedido mapearResultSet(ResultSet rs) throws SQLException {
        Timestamp dataV = rs.getTimestamp("dataVenda");
        return new Pedido(
                rs.getInt("idPedido"),
                dataV != null ? dataV.toLocalDateTime() : null,
                rs.getBigDecimal("total"),
                rs.getString("statusPagamento"),
                rs.getInt("Usuario_idUsuario")
        );
    }
}
