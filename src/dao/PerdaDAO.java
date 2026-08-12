package dao;

import model.Perda;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operações CRUD da tabela Perda (registro de perdas e desperdícios por validade/avaria).
 */
public class PerdaDAO {

    public boolean inserir(Perda perda) {
        String sql = "INSERT INTO Perda (quantidade, dataRegistro, motivo, Lote_idLote) VALUES (?, ?, ?, ?)";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (perda.getQuantidade() != null) {
                stmt.setInt(1, perda.getQuantidade());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }

            if (perda.getDataRegistro() != null) {
                stmt.setTimestamp(2, Timestamp.valueOf(perda.getDataRegistro()));
            } else {
                stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            }

            stmt.setString(3, perda.getMotivo());
            stmt.setInt(4, perda.getLoteId());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        perda.setIdPerda(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inserir Perda: " + e.getMessage());
        }
        return false;
    }

    public Perda buscarPorId(int id) {
        String sql = "SELECT idPerda, quantidade, dataRegistro, motivo, Lote_idLote FROM Perda WHERE idPerda = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar Perda por ID: " + e.getMessage());
        }
        return null;
    }

    public List<Perda> listarTodos() {
        List<Perda> lista = new ArrayList<>();
        String sql = "SELECT idPerda, quantidade, dataRegistro, motivo, Lote_idLote FROM Perda ORDER BY dataRegistro DESC";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar Perdas: " + e.getMessage());
        }
        return lista;
    }

    public List<Perda> listarPorLote(int loteId) {
        List<Perda> lista = new ArrayList<>();
        String sql = "SELECT idPerda, quantidade, dataRegistro, motivo, Lote_idLote FROM Perda WHERE Lote_idLote = ? ORDER BY dataRegistro DESC";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, loteId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar Perdas por Lote: " + e.getMessage());
        }
        return lista;
    }

    public boolean atualizar(Perda perda) {
        String sql = "UPDATE Perda SET quantidade = ?, motivo = ?, Lote_idLote = ? WHERE idPerda = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (perda.getQuantidade() != null) {
                stmt.setInt(1, perda.getQuantidade());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, perda.getMotivo());
            stmt.setInt(3, perda.getLoteId());
            stmt.setInt(4, perda.getIdPerda());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar Perda: " + e.getMessage());
        }
        return false;
    }

    public boolean excluir(int id) {
        String sql = "DELETE FROM Perda WHERE idPerda = ?";
        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir Perda: " + e.getMessage());
        }
        return false;
    }

    private Perda mapearResultSet(ResultSet rs) throws SQLException {
        Timestamp dataReg = rs.getTimestamp("dataRegistro");
        return new Perda(
                rs.getInt("idPerda"),
                rs.getInt("quantidade"),
                dataReg != null ? dataReg.toLocalDateTime() : null,
                rs.getString("motivo"),
                rs.getInt("Lote_idLote")
        );
    }
}
