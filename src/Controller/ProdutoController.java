package controller;

import dao.CategoriaDAO;
import dao.ProdutoCategoriaDAO;
import dao.ProdutoDAO;
import model.Categoria;
import model.Produto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller responsável pelas regras de negócio de Produtos e Catálogo.
 */
public class ProdutoController {

    private final ProdutoDAO produtoDAO;
    private final ProdutoCategoriaDAO produtoCategoriaDAO;
    private final CategoriaDAO categoriaDAO;

    public ProdutoController() {
        this.produtoDAO = new ProdutoDAO();
        this.produtoCategoriaDAO = new ProdutoCategoriaDAO();
        this.categoriaDAO = new CategoriaDAO();
    }

    public ProdutoController(ProdutoDAO produtoDAO, ProdutoCategoriaDAO produtoCategoriaDAO, CategoriaDAO categoriaDAO) {
        this.produtoDAO = produtoDAO;
        this.produtoCategoriaDAO = produtoCategoriaDAO;
        this.categoriaDAO = categoriaDAO;
    }

    public boolean cadastrar(String nome, String marca, BigDecimal precoBase, List<Integer> categoriaIds) {
        if (nome == null || nome.trim().isEmpty()) {
            System.err.println("Erro de validação: O nome do produto é obrigatório.");
            return false;
        }
        if (precoBase == null || precoBase.compareTo(BigDecimal.ZERO) < 0) {
            System.err.println("Erro de validação: O preço base não pode ser negativo.");
            return false;
        }

        Produto produto = new Produto(
                0,
                nome.trim(),
                marca != null ? marca.trim() : null,
                precoBase,
                "ATIVO"
        );

        boolean inseriu = produtoDAO.inserir(produto);
        if (inseriu && categoriaIds != null) {
            for (Integer catId : categoriaIds) {
                if (catId != null && catId > 0) {
                    produtoCategoriaDAO.associar(produto.getIdProduto(), catId);
                }
            }
        }
        return inseriu;
    }

    public Produto buscarPorId(int id) {
        if (id <= 0) {
            return null;
        }
        Produto produto = produtoDAO.buscarPorId(id);
        if (produto != null) {
            List<Categoria> categorias = categoriaDAO.listarPorProduto(id);
            produto.setCategorias(categorias);
        }
        return produto;
    }

    public List<Produto> listarTodos() {
        List<Produto> produtos = produtoDAO.listarTodos();
        for (Produto p : produtos) {
            p.setCategorias(categoriaDAO.listarPorProduto(p.getIdProduto()));
        }
        return produtos;
    }

    /**
     * Lista apenas produtos da vitrine (ativos com lote válido e saldo positivo)
     */
    public List<Produto> listarVitrine() {
        List<Produto> produtos = produtoDAO.listarVitrine();
        for (Produto p : produtos) {
            p.setCategorias(categoriaDAO.listarPorProduto(p.getIdProduto()));
        }
        return produtos;
    }

    public boolean atualizar(int id, String nome, String marca, BigDecimal precoBase, String status, List<Integer> novasCategorias) {
        Produto existente = produtoDAO.buscarPorId(id);
        if (existente == null) {
            System.err.println("Erro: Produto com ID " + id + " não encontrado.");
            return false;
        }

        if (nome != null && !nome.trim().isEmpty()) {
            existente.setNome(nome.trim());
        }
        if (marca != null) {
            existente.setMarca(marca.trim());
        }
        if (precoBase != null && precoBase.compareTo(BigDecimal.ZERO) >= 0) {
            existente.setPrecoBase(precoBase);
        }
        if (status != null && !status.trim().isEmpty()) {
            existente.setStatus(status.trim().toUpperCase());
        }

        boolean atualizou = produtoDAO.atualizar(existente);

        if (atualizou && novasCategorias != null) {
            produtoCategoriaDAO.desassociarTodasDoProduto(id);
            for (Integer catId : novasCategorias) {
                if (catId != null && catId > 0) {
                    produtoCategoriaDAO.associar(id, catId);
                }
            }
        }

        return atualizou;
    }

    public boolean inativar(int id) {
        if (id <= 0) {
            return false;
        }
        return produtoDAO.inativar(id);
    }

    public boolean excluir(int id) {
        if (id <= 0) {
            return false;
        }
        return produtoDAO.excluir(id);
    }
}
