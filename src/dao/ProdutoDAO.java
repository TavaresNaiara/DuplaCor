package dao;

import model.Produto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operações CRUD da tabela Produto.
 */
public class ProdutoDAO {

    public boolean inserir(Produto produto) {
        String sql = "INSERT INTO Produto (nome, marca, precoBase, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, produto.getNome());
            stmt.setString(2, produto.getMarca());
            stmt.setBigDecimal(3, produto.getPrecoBase());
            stmt.setString(4, produto.getStatus() != null ? produto.getStatus() : "ATIVO");

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        produto.setIdProduto(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inserir Produto: " + e.getMessage());
        }
        return false;
    }

    public Produto buscarPorId(int id) {
        String sql = "SELECT idProduto, nome, marca, precoBase, status FROM Produto WHERE idProduto = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar Produto por ID: " + e.getMessage());
        }
        return null;
    }

    public List<Produto> listarTodos() {
        List<Produto> lista = new ArrayList<>();
        String sql = "SELECT idProduto, nome, marca, precoBase, status FROM Produto ORDER BY idProduto ASC";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar Produtos: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Lista apenas os produtos ATIVOS que possuem lotes disponíveis com saldo e dentro da validade (Vitrine)
     */
    public List<Produto> listarVitrine() {
        List<Produto> lista = new ArrayList<>();
        String sql = "SELECT DISTINCT p.idProduto, p.nome, p.marca, p.precoBase, p.status " +
                     "FROM Produto p " +
                     "INNER JOIN Lote l ON p.idProduto = l.Produto_idProduto " +
                     "WHERE p.status = 'ATIVO' AND l.status = 'DISPONIVEL' " +
                     "AND l.quantAtual > 0 AND l.dataValidade >= CURDATE() " +
                     "ORDER BY p.nome ASC";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar Produtos na Vitrine: " + e.getMessage());
        }
        return lista;
    }

    public boolean atualizar(Produto produto) {
        String sql = "UPDATE Produto SET nome = ?, marca = ?, precoBase = ?, status = ? WHERE idProduto = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, produto.getNome());
            stmt.setString(2, produto.getMarca());
            stmt.setBigDecimal(3, produto.getPrecoBase());
            stmt.setString(4, produto.getStatus());
            stmt.setInt(5, produto.getIdProduto());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar Produto: " + e.getMessage());
        }
        return false;
    }

    public boolean inativar(int id) {
        String sql = "UPDATE Produto SET status = 'INATIVO' WHERE idProduto = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao inativar Produto: " + e.getMessage());
        }
        return false;
    }

    public boolean excluir(int id) {
        String sql = "DELETE FROM Produto WHERE idProduto = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir Produto: " + e.getMessage());
        }
        return false;
    }

    private Produto mapearResultSet(ResultSet rs) throws SQLException {
        return new Produto(
                rs.getInt("idProduto"),
                rs.getString("nome"),
                rs.getString("marca"),
                rs.getBigDecimal("precoBase"),
                rs.getString("status")
        );
    }
}
