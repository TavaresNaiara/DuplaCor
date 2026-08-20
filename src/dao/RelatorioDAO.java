package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO de consultas agregadas para o módulo "Gestão de Relatórios e Métricas"
 * (item 8 do escopo). Diferente dos demais DAOs, este não mapeia uma tabela
 * específica: ele cruza Pedido, ItemPedido, Lote, Produto, Categoria e Perda
 * para produzir indicadores de negócio.
 */
public class RelatorioDAO {

    /**
     * Relatório de Vendas: faturamento e quantidade vendida por produto,
     * ordenado do mais vendido para o menos vendido.
     */
    public List<Map<String, Object>> relatorioVendasPorProduto() {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql =
                "SELECT p.idProduto, p.nome, p.marca, " +
                "       COALESCE(SUM(ip.quantidade), 0) AS quantidadeVendida, " +
                "       COALESCE(SUM(ip.quantidade * ip.precoAplicado), 0) AS faturamento " +
                "FROM Produto p " +
                "LEFT JOIN Lote l ON l.Produto_idProduto = p.idProduto " +
                "LEFT JOIN ItemPedido ip ON ip.Lote_idLote = l.idLote " +
                "LEFT JOIN Pedido ped ON ped.idPedido = ip.Pedido_idPedido AND ped.statusPagamento = 'PAGO' " +
                "GROUP BY p.idProduto, p.nome, p.marca " +
                "ORDER BY faturamento DESC";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> linha = new LinkedHashMap<>();
                linha.put("idProduto", rs.getInt("idProduto"));
                linha.put("nome", rs.getString("nome"));
                linha.put("marca", rs.getString("marca"));
                linha.put("quantidadeVendida", rs.getInt("quantidadeVendida"));
                linha.put("faturamento", rs.getBigDecimal("faturamento"));
                lista.add(linha);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao gerar Relatório de Vendas: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Controle de Estoque: saldo atual agrupado por produto, com a validade
     * mais próxima entre os lotes disponíveis daquele produto (é o próximo
     * lote que o FEFO vai despachar).
     */
    public List<Map<String, Object>> relatorioEstoquePorProduto() {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql =
                "SELECT p.idProduto, p.nome, p.marca, " +
                "       COALESCE(SUM(CASE WHEN l.status = 'DISPONIVEL' THEN l.quantAtual ELSE 0 END), 0) AS saldoDisponivel, " +
                "       MIN(CASE WHEN l.status = 'DISPONIVEL' AND l.quantAtual > 0 THEN l.dataValidade END) AS proximaValidade " +
                "FROM Produto p " +
                "LEFT JOIN Lote l ON l.Produto_idProduto = p.idProduto " +
                "GROUP BY p.idProduto, p.nome, p.marca " +
                "ORDER BY p.nome ASC";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> linha = new LinkedHashMap<>();
                linha.put("idProduto", rs.getInt("idProduto"));
                linha.put("nome", rs.getString("nome"));
                linha.put("marca", rs.getString("marca"));
                linha.put("saldoDisponivel", rs.getInt("saldoDisponivel"));
                Date validade = rs.getDate("proximaValidade");
                linha.put("proximaValidade", validade != null ? validade.toLocalDate().toString() : null);
                lista.add(linha);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao gerar Relatório de Estoque: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Análise de Consumo: quantidade vendida e faturamento agrupados por
     * categoria, para identificar padrões de compra das usuárias.
     */
    public List<Map<String, Object>> relatorioConsumoPorCategoria() {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql =
                "SELECT c.idCategoria, c.nome, " +
                "       COALESCE(SUM(ip.quantidade), 0) AS quantidadeVendida, " +
                "       COALESCE(SUM(ip.quantidade * ip.precoAplicado), 0) AS faturamento " +
                "FROM Categoria c " +
                "LEFT JOIN Produto_has_Categoria phc ON phc.Categoria_idCategoria = c.idCategoria " +
                "LEFT JOIN Lote l ON l.Produto_idProduto = phc.Produto_idProduto " +
                "LEFT JOIN ItemPedido ip ON ip.Lote_idLote = l.idLote " +
                "LEFT JOIN Pedido ped ON ped.idPedido = ip.Pedido_idPedido AND ped.statusPagamento = 'PAGO' " +
                "GROUP BY c.idCategoria, c.nome " +
                "ORDER BY faturamento DESC";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> linha = new LinkedHashMap<>();
                linha.put("idCategoria", rs.getInt("idCategoria"));
                linha.put("nome", rs.getString("nome"));
                linha.put("quantidadeVendida", rs.getInt("quantidadeVendida"));
                linha.put("faturamento", rs.getBigDecimal("faturamento"));
                lista.add(linha);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao gerar Análise de Consumo: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Estatísticas Gerais: indicadores de topo para o dashboard administrativo.
     */
    public Map<String, Object> estatisticasGerais() {
        Map<String, Object> stats = new LinkedHashMap<>();
        String sql =
                "SELECT " +
                "  (SELECT COUNT(*) FROM Produto WHERE status = 'ATIVO') AS produtosAtivos, " +
                "  (SELECT COUNT(*) FROM Lote WHERE status = 'DISPONIVEL') AS lotesDisponiveis, " +
                "  (SELECT COALESCE(SUM(quantAtual), 0) FROM Lote WHERE status = 'DISPONIVEL') AS unidadesEmEstoque, " +
                "  (SELECT COUNT(*) FROM Pedido WHERE statusPagamento = 'PAGO') AS totalPedidos, " +
                "  (SELECT COALESCE(SUM(total), 0) FROM Pedido WHERE statusPagamento = 'PAGO') AS faturamentoTotal, " +
                "  (SELECT COALESCE(SUM(quantidade), 0) FROM Perda) AS unidadesPerdidas, " +
                "  (SELECT COUNT(*) FROM Lote WHERE status = 'VENCIDO') AS lotesVencidos";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                stats.put("produtosAtivos", rs.getInt("produtosAtivos"));
                stats.put("lotesDisponiveis", rs.getInt("lotesDisponiveis"));
                stats.put("unidadesEmEstoque", rs.getInt("unidadesEmEstoque"));
                stats.put("totalPedidos", rs.getInt("totalPedidos"));
                stats.put("faturamentoTotal", rs.getBigDecimal("faturamentoTotal"));
                stats.put("unidadesPerdidas", rs.getInt("unidadesPerdidas"));
                stats.put("lotesVencidos", rs.getInt("lotesVencidos"));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao gerar Estatísticas Gerais: " + e.getMessage());
        }
        return stats;
    }
}
