package dao;

import model.ItemPedido;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operações CRUD da tabela ItemPedido.
 */
public class ItemPedidoDAO {

    public boolean inserir(ItemPedido item) {
        String sql = "INSERT INTO ItemPedido (quantidade, precoAplicado, Lote_idLote, Pedido_idPedido) VALUES (?, ?, ?, ?)";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, item.getQuantidade());
            stmt.setBigDecimal(2, item.getPrecoAplicado());
            stmt.setInt(3, item.getLoteId());
            stmt.setInt(4, item.getPedidoId());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        item.setIdItemPedido(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inserir ItemPedido: " + e.getMessage());
        }
        return false;
    }

    public ItemPedido buscarPorId(int idItemPedido, int idPedido) {
        String sql = "SELECT idItemPedido, quantidade, precoAplicado, Lote_idLote, Pedido_idPedido FROM ItemPedido " +
                     "WHERE idItemPedido = ? AND Pedido_idPedido = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idItemPedido);
            stmt.setInt(2, idPedido);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar ItemPedido por ID: " + e.getMessage());
        }
        return null;
    }

    public List<ItemPedido> listarPorPedido(int idPedido) {
        List<ItemPedido> lista = new ArrayList<>();
        String sql = "SELECT idItemPedido, quantidade, precoAplicado, Lote_idLote, Pedido_idPedido FROM ItemPedido " +
                     "WHERE Pedido_idPedido = ? ORDER BY idItemPedido ASC";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPedido);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar Itens por Pedido: " + e.getMessage());
        }
        return lista;
    }

    public List<ItemPedido> listarTodos() {
        List<ItemPedido> lista = new ArrayList<>();
        String sql = "SELECT idItemPedido, quantidade, precoAplicado, Lote_idLote, Pedido_idPedido FROM ItemPedido ORDER BY idItemPedido ASC";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar todos os Itens de Pedidos: " + e.getMessage());
        }
        return lista;
    }

    public boolean atualizar(ItemPedido item) {
        String sql = "UPDATE ItemPedido SET quantidade = ?, precoAplicado = ?, Lote_idLote = ? " +
                     "WHERE idItemPedido = ? AND Pedido_idPedido = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, item.getQuantidade());
            stmt.setBigDecimal(2, item.getPrecoAplicado());
            stmt.setInt(3, item.getLoteId());
            stmt.setInt(4, item.getIdItemPedido());
            stmt.setInt(5, item.getPedidoId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar ItemPedido: " + e.getMessage());
        }
        return false;
    }

    public boolean excluir(int idItemPedido, int idPedido) {
        String sql = "DELETE FROM ItemPedido WHERE idItemPedido = ? AND Pedido_idPedido = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idItemPedido);
            stmt.setInt(2, idPedido);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir ItemPedido: " + e.getMessage());
        }
        return false;
    }

    public boolean excluirPorPedido(int idPedido) {
        String sql = "DELETE FROM ItemPedido WHERE Pedido_idPedido = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPedido);
            return stmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir Itens do Pedido: " + e.getMessage());
        }
        return false;
    }

    private ItemPedido mapearResultSet(ResultSet rs) throws SQLException {
        return new ItemPedido(
                rs.getInt("idItemPedido"),
                rs.getInt("quantidade"),
                rs.getBigDecimal("precoAplicado"),
                rs.getInt("Lote_idLote"),
                rs.getInt("Pedido_idPedido")
        );
    }
}
