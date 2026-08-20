package controller;

import dao.RelatorioDAO;

import java.util.List;
import java.util.Map;

/**
 * Controller responsável pelo módulo "Gestão de Relatórios e Métricas"
 * (item 8 do escopo): vendas, estoque, perdas, consumo e estatísticas gerais.
 * O Relatório de Perdas em si já é coberto por {@link PerdaController}.
 */
public class RelatorioController {

    private final RelatorioDAO relatorioDAO;

    public RelatorioController() {
        this.relatorioDAO = new RelatorioDAO();
    }

    public RelatorioController(RelatorioDAO relatorioDAO) {
        this.relatorioDAO = relatorioDAO;
    }

    public List<Map<String, Object>> relatorioVendas() {
        return relatorioDAO.relatorioVendasPorProduto();
    }

    public List<Map<String, Object>> relatorioEstoque() {
        return relatorioDAO.relatorioEstoquePorProduto();
    }

    public List<Map<String, Object>> relatorioConsumo() {
        return relatorioDAO.relatorioConsumoPorCategoria();
    }

    public Map<String, Object> estatisticasGerais() {
        return relatorioDAO.estatisticasGerais();
    }
}
