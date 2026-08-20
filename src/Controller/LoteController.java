package controller;

import dao.LoteDAO;
import dao.PerdaDAO;
import dao.ProdutoDAO;
import model.Lote;
import model.Perda;
import model.Produto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller responsável pelas regras de negócio de Lotes e Algoritmo FEFO.
 */
public class LoteController {

    private final LoteDAO loteDAO;
    private final ProdutoDAO produtoDAO;
    private final PerdaDAO perdaDAO;

    public LoteController() {
        this.loteDAO = new LoteDAO();
        this.produtoDAO = new ProdutoDAO();
        this.perdaDAO = new PerdaDAO();
    }

    public LoteController(LoteDAO loteDAO, ProdutoDAO produtoDAO, PerdaDAO perdaDAO) {
        this.loteDAO = loteDAO;
        this.produtoDAO = produtoDAO;
        this.perdaDAO = perdaDAO;
    }

    public boolean cadastrar(int quantInicial, LocalDate dataValidade, LocalDate dataEntrada, int produtoId) {
        if (quantInicial <= 0) {
            System.err.println("Erro de validação: A quantidade inicial do lote deve ser maior que zero.");
            return false;
        }
        if (dataValidade == null) {
            System.err.println("Erro de validação: A data de validade é obrigatória.");
            return false;
        }
        if (dataEntrada == null) {
            dataEntrada = LocalDate.now();
        }
        if (dataValidade.isBefore(dataEntrada)) {
            System.err.println("Erro de validação: A data de validade não pode ser anterior à data de entrada.");
            return false;
        }

        Produto produto = produtoDAO.buscarPorId(produtoId);
        if (produto == null) {
            System.err.println("Erro: Produto de ID " + produtoId + " não encontrado.");
            return false;
        }

        String status = dataValidade.isBefore(LocalDate.now()) ? "VENCIDO" : "DISPONIVEL";

        Lote lote = new Lote(
                0,
                quantInicial,
                quantInicial, // quantAtual começa igual a quantInicial
                dataValidade,
                dataEntrada,
                status,
                produtoId
        );

        return loteDAO.inserir(lote);
    }

    public Lote buscarPorId(int id) {
        if (id <= 0) {
            return null;
        }
        Lote lote = loteDAO.buscarPorId(id);
        if (lote != null) {
            lote.setProduto(produtoDAO.buscarPorId(lote.getProdutoId()));
        }
        return lote;
    }

    public List<Lote> listarTodos() {
        List<Lote> lotes = loteDAO.listarTodos();
        for (Lote l : lotes) {
            l.setProduto(produtoDAO.buscarPorId(l.getProdutoId()));
        }
        return lotes;
    }

    public List<Lote> listarPorProduto(int produtoId) {
        List<Lote> lotes = loteDAO.listarPorProduto(produtoId);
        Produto prod = produtoDAO.buscarPorId(produtoId);
        for (Lote l : lotes) {
            l.setProduto(prod);
        }
        return lotes;
    }

    /**
     * Retorna os lotes disponíveis para venda ordenados pelo Algoritmo FEFO (validade mais próxima primeiro).
     */
    public List<Lote> listarDisponiveisFEFO(int produtoId) {
        List<Lote> lotes = loteDAO.listarDisponiveisFEFO(produtoId);
        Produto prod = produtoDAO.buscarPorId(produtoId);
        for (Lote l : lotes) {
            l.setProduto(prod);
        }
        return lotes;
    }

    /**
     * Rotina de monitoramento: bloqueia lotes vencidos e registra automaticamente
     * a perda do saldo restante, conforme o escopo ("Registro de Perdas: Inclusão
     * automática de lotes expirados"). Um lote vencido sai da vitrine e todo o
     * saldo que não foi comercializado vira um registro no Relatório de Perdas.
     */
    public int monitorarEBloquearVencidos() {
        List<Lote> vencidos = loteDAO.listarVencidos();
        int bloqueados = 0;
        for (Lote l : vencidos) {
            if (!"VENCIDO".equalsIgnoreCase(l.getStatus())) {
                if (l.getQuantAtual() > 0) {
                    Perda perda = new Perda(
                            0,
                            l.getQuantAtual(),
                            LocalDateTime.now(),
                            "PRODUTO VENCIDO",
                            l.getIdLote()
                    );
                    perdaDAO.inserir(perda);
                    loteDAO.atualizarQuantidade(l.getIdLote(), 0);
                }
                loteDAO.atualizarStatus(l.getIdLote(), "VENCIDO");
                bloqueados++;
            }
        }
        return bloqueados;
    }

    public boolean atualizar(int id, int quantAtual, LocalDate dataValidade, LocalDate dataEntrada, String status) {
        Lote existente = loteDAO.buscarPorId(id);
        if (existente == null) {
            System.err.println("Erro: Lote com ID " + id + " não encontrado.");
            return false;
        }

        if (quantAtual >= 0) {
            existente.setQuantAtual(quantAtual);
        }
        if (dataValidade != null) {
            existente.setDataValidade(dataValidade);
        }
        if (dataEntrada != null) {
            existente.setDataEntrada(dataEntrada);
        }
        if (status != null && !status.trim().isEmpty()) {
            existente.setStatus(status.trim().toUpperCase());
        }

        return loteDAO.atualizar(existente);
    }

    public boolean excluir(int id) {
        if (id <= 0) {
            return false;
        }
        return loteDAO.excluir(id);
    }
}
