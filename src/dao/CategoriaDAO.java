package dao;

import model.Categoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operações CRUD da tabela Categoria.
 */
public class CategoriaDAO {

    public boolean inserir(Categoria categoria) {
        String sql = "INSERT INTO Categoria (nome, descricao) VALUES (?, ?)";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, categoria.getNome());
            stmt.setString(2, categoria.getDescricao());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        categoria.setIdCategoria(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inserir Categoria: " + e.getMessage());
        }
        return false;
    }

    public Categoria buscarPorId(int id) {
        String sql = "SELECT idCategoria, nome, descricao FROM Categoria WHERE idCategoria = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar Categoria por ID: " + e.getMessage());
        }
        return null;
    }

    public List<Categoria> listarTodos() {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT idCategoria, nome, descricao FROM Categoria ORDER BY nome ASC";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar Categorias: " + e.getMessage());
        }
        return lista;
    }

    public List<Categoria> listarPorProduto(int produtoId) {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT c.idCategoria, c.nome, c.descricao FROM Categoria c " +
                     "INNER JOIN Produto_has_Categoria pc ON c.idCategoria = pc.Categoria_idCategoria " +
                     "WHERE pc.Produto_idProduto = ? ORDER BY c.nome ASC";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, produtoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar Categorias por Produto: " + e.getMessage());
        }
        return lista;
    }

    public boolean atualizar(Categoria categoria) {
        String sql = "UPDATE Categoria SET nome = ?, descricao = ? WHERE idCategoria = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categoria.getNome());
            stmt.setString(2, categoria.getDescricao());
            stmt.setInt(3, categoria.getIdCategoria());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar Categoria: " + e.getMessage());
        }
        return false;
    }

    public boolean excluir(int id) {
        String sql = "DELETE FROM Categoria WHERE idCategoria = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir Categoria: " + e.getMessage());
        }
        return false;
    }

    private Categoria mapearResultSet(ResultSet rs) throws SQLException {
        return new Categoria(
                rs.getInt("idCategoria"),
                rs.getString("nome"),
                rs.getString("descricao")
        );
    }
}
