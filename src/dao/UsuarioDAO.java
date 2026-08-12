package dao;

import model.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operações CRUD da tabela Usuario.
 */
public class UsuarioDAO {

    public boolean inserir(Usuario usuario) {
        String sql = "INSERT INTO Usuario (nome, email, senha, perfil, dataCadastro, tokenRecuperacao) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getSenha());
            stmt.setString(4, usuario.getPerfil() != null ? usuario.getPerfil() : "CLIENTE");

            if (usuario.getDataCadastro() != null) {
                stmt.setTimestamp(5, Timestamp.valueOf(usuario.getDataCadastro()));
            } else {
                stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            }

            stmt.setString(6, usuario.getTokenRecuperacao());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        usuario.setIdUsuario(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inserir Usuario: " + e.getMessage());
        }
        return false;
    }

    public Usuario buscarPorId(int id) {
        String sql = "SELECT idUsuario, nome, email, senha, perfil, dataCadastro, tokenRecuperacao FROM Usuario WHERE idUsuario = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar Usuario por ID: " + e.getMessage());
        }
        return null;
    }

    public Usuario buscarPorEmail(String email) {
        String sql = "SELECT idUsuario, nome, email, senha, perfil, dataCadastro, tokenRecuperacao FROM Usuario WHERE email = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar Usuario por email: " + e.getMessage());
        }
        return null;
    }

    public Usuario autenticar(String email, String senha) {
        String sql = "SELECT idUsuario, nome, email, senha, perfil, dataCadastro, tokenRecuperacao FROM Usuario WHERE email = ? AND senha = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, senha);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao autenticar Usuario: " + e.getMessage());
        }
        return null;
    }

    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT idUsuario, nome, email, senha, perfil, dataCadastro, tokenRecuperacao FROM Usuario ORDER BY idUsuario ASC";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar Usuarios: " + e.getMessage());
        }
        return lista;
    }

    public boolean atualizar(Usuario usuario) {
        String sql = "UPDATE Usuario SET nome = ?, email = ?, senha = ?, perfil = ?, tokenRecuperacao = ? WHERE idUsuario = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getSenha());
            stmt.setString(4, usuario.getPerfil());
            stmt.setString(5, usuario.getTokenRecuperacao());
            stmt.setInt(6, usuario.getIdUsuario());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar Usuario: " + e.getMessage());
        }
        return false;
    }

    public boolean excluir(int id) {
        String sql = "DELETE FROM Usuario WHERE idUsuario = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir Usuario: " + e.getMessage());
        }
        return false;
    }

    private Usuario mapearResultSet(ResultSet rs) throws SQLException {
        Timestamp dataCad = rs.getTimestamp("dataCadastro");
        return new Usuario(
                rs.getInt("idUsuario"),
                rs.getString("nome"),
                rs.getString("email"),
                rs.getString("senha"),
                rs.getString("perfil"),
                dataCad != null ? dataCad.toLocalDateTime() : null,
                rs.getString("tokenRecuperacao")
        );
    }
}
