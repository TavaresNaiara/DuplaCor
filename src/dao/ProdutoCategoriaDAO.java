package dao;

import model.ProdutoCategoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operações da tabela associativa Produto_has_Categoria (N:N).
 */
public class ProdutoCategoriaDAO {

    public boolean associar(int produtoId, int categoriaId) {
        String sql = "INSERT INTO Produto_has_Categoria (Produto_idProduto, Categoria_idCategoria) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE Produto_idProduto = Produto_idProduto";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, produtoId);
            stmt.setInt(2, categoriaId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao associar Produto à Categoria: " + e.getMessage());
        }
        return false;
    }

    public boolean desassociar(int produtoId, int categoriaId) {
        String sql = "DELETE FROM Produto_has_Categoria WHERE Produto_idProduto = ? AND Categoria_idCategoria = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, produtoId);
            stmt.setInt(2, categoriaId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao desassociar Produto de Categoria: " + e.getMessage());
        }
        return false;
    }

    public boolean desassociarTodasDoProduto(int produtoId) {
        String sql = "DELETE FROM Produto_has_Categoria WHERE Produto_idProduto = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, produtoId);
            return stmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            System.err.println("Erro ao desassociar todas as categorias do produto: " + e.getMessage());
        }
        return false;
    }

    public List<ProdutoCategoria> listarTodas() {
        List<ProdutoCategoria> lista = new ArrayList<>();
        String sql = "SELECT Produto_idProduto, Categoria_idCategoria FROM Produto_has_Categoria";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new ProdutoCategoria(
                        rs.getInt("Produto_idProduto"),
                        rs.getInt("Categoria_idCategoria")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar associações Produto-Categoria: " + e.getMessage());
        }
        return lista;
    }

    public List<Integer> listarCategoriasPorProduto(int produtoId) {
        List<Integer> lista = new ArrayList<>();
        String sql = "SELECT Categoria_idCategoria FROM Produto_has_Categoria WHERE Produto_idProduto = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, produtoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(rs.getInt("Categoria_idCategoria"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar IDs de categorias do produto: " + e.getMessage());
        }
        return lista;
    }

    public List<Integer> listarProdutosPorCategoria(int categoriaId) {
        List<Integer> lista = new ArrayList<>();
        String sql = "SELECT Produto_idProduto FROM Produto_has_Categoria WHERE Categoria_idCategoria = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoriaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(rs.getInt("Produto_idProduto"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar IDs de produtos da categoria: " + e.getMessage());
        }
        return lista;
    }
}
