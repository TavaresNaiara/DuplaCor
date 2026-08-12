package dao;

import model.UsuarioCarrinho;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operações CRUD da tabela UsuarioCarrinho (Carrinho Persistente).
 */
public class UsuarioCarrinhoDAO {

    public boolean inserir(UsuarioCarrinho item) {
        String sql = "INSERT INTO UsuarioCarrinho (dataAdicao, quantidade, Usuario_idUsuario, Produto_idProduto) VALUES (?, ?, ?, ?)";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (item.getDataAdicao() != null) {
                stmt.setTimestamp(1, Timestamp.valueOf(item.getDataAdicao()));
            } else {
                stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            }

            stmt.setInt(2, item.getQuantidade());
            stmt.setInt(3, item.getUsuarioId());
            stmt.setInt(4, item.getProdutoId());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        item.setIdUsuarioCarrinho(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inserir Item no Carrinho: " + e.getMessage());
        }
        return false;
    }

    public boolean adicionarOuIncrementar(int usuarioId, int produtoId, int quantidade) {
        UsuarioCarrinho existente = buscarPorUsuarioEProduto(usuarioId, produtoId);
        if (existente != null) {
            int novaQtd = existente.getQuantidade() + quantidade;
            return atualizarQuantidade(existente.getIdUsuarioCarrinho(), novaQtd);
        } else {
            UsuarioCarrinho novo = new UsuarioCarrinho(0, java.time.LocalDateTime.now(), quantidade, usuarioId, produtoId);
            return inserir(novo);
        }
    }

    public UsuarioCarrinho buscarPorId(int id) {
        String sql = "SELECT idUsuarioCarrinho, dataAdicao, quantidade, Usuario_idUsuario, Produto_idProduto FROM UsuarioCarrinho WHERE idUsuarioCarrinho = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar Item do Carrinho por ID: " + e.getMessage());
        }
        return null;
    }

    public UsuarioCarrinho buscarPorUsuarioEProduto(int usuarioId, int produtoId) {
        String sql = "SELECT idUsuarioCarrinho, dataAdicao, quantidade, Usuario_idUsuario, Produto_idProduto " +
                     "FROM UsuarioCarrinho WHERE Usuario_idUsuario = ? AND Produto_idProduto = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);
            stmt.setInt(2, produtoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar Item do Carrinho por Usuário e Produto: " + e.getMessage());
        }
        return null;
    }

    public List<UsuarioCarrinho> listarPorUsuario(int usuarioId) {
        List<UsuarioCarrinho> lista = new ArrayList<>();
        String sql = "SELECT idUsuarioCarrinho, dataAdicao, quantidade, Usuario_idUsuario, Produto_idProduto " +
                     "FROM UsuarioCarrinho WHERE Usuario_idUsuario = ? ORDER BY dataAdicao DESC";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar Itens do Carrinho por Usuário: " + e.getMessage());
        }
        return lista;
    }

    public List<UsuarioCarrinho> listarTodos() {
        List<UsuarioCarrinho> lista = new ArrayList<>();
        String sql = "SELECT idUsuarioCarrinho, dataAdicao, quantidade, Usuario_idUsuario, Produto_idProduto FROM UsuarioCarrinho ORDER BY idUsuarioCarrinho ASC";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar todos os Itens do Carrinho: " + e.getMessage());
        }
        return lista;
    }

    public boolean atualizarQuantidade(int idUsuarioCarrinho, int quantidade) {
        String sql = "UPDATE UsuarioCarrinho SET quantidade = ?, dataAdicao = NOW() WHERE idUsuarioCarrinho = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantidade);
            stmt.setInt(2, idUsuarioCarrinho);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar quantidade do Carrinho: " + e.getMessage());
        }
        return false;
    }

    public boolean excluir(int idUsuarioCarrinho) {
        String sql = "DELETE FROM UsuarioCarrinho WHERE idUsuarioCarrinho = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuarioCarrinho);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir Item do Carrinho: " + e.getMessage());
        }
        return false;
    }

    public boolean limparCarrinhoDoUsuario(int usuarioId) {
        String sql = "DELETE FROM UsuarioCarrinho WHERE Usuario_idUsuario = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);
            return stmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            System.err.println("Erro ao limpar Carrinho do Usuário: " + e.getMessage());
        }
        return false;
    }

    private UsuarioCarrinho mapearResultSet(ResultSet rs) throws SQLException {
        Timestamp dataAd = rs.getTimestamp("dataAdicao");
        return new UsuarioCarrinho(
                rs.getInt("idUsuarioCarrinho"),
                dataAd != null ? dataAd.toLocalDateTime() : null,
                rs.getInt("quantidade"),
                rs.getInt("Usuario_idUsuario"),
                rs.getInt("Produto_idProduto")
        );
    }
}
