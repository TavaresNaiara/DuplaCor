package controller;

import dao.LoteDAO;
import dao.PerdaDAO;
import dao.ProdutoDAO;
import model.Lote;
import model.Perda;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller responsável pelas regras de auditoria e registro de Perdas (desperdício/validade).
 */
public class PerdaController {

    private final PerdaDAO perdaDAO;
    private final LoteDAO loteDAO;
    private final ProdutoDAO produtoDAO;

    public PerdaController() {
        this.perdaDAO = new PerdaDAO();
        this.loteDAO = new LoteDAO();
        this.produtoDAO = new ProdutoDAO();
    }

    public PerdaController(PerdaDAO perdaDAO, LoteDAO loteDAO, ProdutoDAO produtoDAO) {
        this.perdaDAO = perdaDAO;
        this.loteDAO = loteDAO;
        this.produtoDAO = produtoDAO;
    }

    /**
     * Registra uma perda e realiza a respectiva baixa no estoque do Lote.
     */
    public boolean registrarPerda(int loteId, int quantidade, String motivo) {
        if (quantidade <= 0) {
            System.err.println("Erro de validação: A quantidade perdida deve ser maior que zero.");
            return false;
        }

        Lote lote = loteDAO.buscarPorId(loteId);
        if (lote == null) {
            System.err.println("Erro: Lote de ID " + loteId + " não encontrado.");
            return false;
        }

        if (lote.getQuantAtual() < quantidade) {
            System.err.println("Erro: A quantidade informada (" + quantidade + ") é maior que o saldo atual do lote (" + lote.getQuantAtual() + ").");
            return false;
        }

        Perda perda = new Perda(
                0,
                quantidade,
                LocalDateTime.now(),
                motivo != null ? motivo.trim().toUpperCase() : "DESCARTE",
                loteId
        );

        boolean inseriu = perdaDAO.inserir(perda);
        if (inseriu) {
            // Dar baixa no saldo do lote
            int novoSaldo = lote.getQuantAtual() - quantidade;
            loteDAO.atualizarQuantidade(loteId, novoSaldo);
            if (novoSaldo == 0 && "PRODUTO VENCIDO".equalsIgnoreCase(motivo)) {
                loteDAO.atualizarStatus(loteId, "VENCIDO");
            }
        }
        return inseriu;
    }

    public Perda buscarPorId(int id) {
        if (id <= 0) {
            return null;
        }
        Perda perda = perdaDAO.buscarPorId(id);
        if (perda != null) {
            Lote lote = loteDAO.buscarPorId(perda.getLoteId());
            if (lote != null) {
                lote.setProduto(produtoDAO.buscarPorId(lote.getProdutoId()));
                perda.setLote(lote);
            }
        }
        return perda;
    }

    public List<Perda> listarTodos() {
        List<Perda> perdas = perdaDAO.listarTodos();
        for (Perda p : perdas) {
            Lote lote = loteDAO.buscarPorId(p.getLoteId());
            if (lote != null) {
                lote.setProduto(produtoDAO.buscarPorId(lote.getProdutoId()));
                p.setLote(lote);
            }
        }
        return perdas;
    }

    public List<Perda> listarPorLote(int loteId) {
        List<Perda> perdas = perdaDAO.listarPorLote(loteId);
        Lote lote = loteDAO.buscarPorId(loteId);
        if (lote != null) {
            lote.setProduto(produtoDAO.buscarPorId(lote.getProdutoId()));
            for (Perda p : perdas) {
                p.setLote(lote);
            }
        }
        return perdas;
    }

    public boolean excluir(int id) {
        if (id <= 0) {
            return false;
        }
        return perdaDAO.excluir(id);
    }
}
