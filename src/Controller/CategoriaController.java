package controller;

import dao.CategoriaDAO;
import model.Categoria;

import java.util.List;

/**
 * Controller responsável pelas regras de negócio de Categorias.
 */
public class CategoriaController {

    private final CategoriaDAO categoriaDAO;

    public CategoriaController() {
        this.categoriaDAO = new CategoriaDAO();
    }

    public CategoriaController(CategoriaDAO categoriaDAO) {
        this.categoriaDAO = categoriaDAO;
    }

    public boolean cadastrar(String nome, String descricao) {
        if (nome == null || nome.trim().isEmpty()) {
            System.err.println("Erro de validação: O nome da categoria é obrigatório.");
            return false;
        }

        Categoria categoria = new Categoria(
                0,
                nome.trim(),
                descricao != null ? descricao.trim() : null
        );

        return categoriaDAO.inserir(categoria);
    }

    public Categoria buscarPorId(int id) {
        if (id <= 0) {
            return null;
        }
        return categoriaDAO.buscarPorId(id);
    }

    public List<Categoria> listarTodos() {
        return categoriaDAO.listarTodos();
    }

    public List<Categoria> listarPorProduto(int produtoId) {
        if (produtoId <= 0) {
            return List.of();
        }
        return categoriaDAO.listarPorProduto(produtoId);
    }

    public boolean atualizar(int id, String nome, String descricao) {
        Categoria existente = categoriaDAO.buscarPorId(id);
        if (existente == null) {
            System.err.println("Erro: Categoria com ID " + id + " não encontrada.");
            return false;
        }

        if (nome != null && !nome.trim().isEmpty()) {
            existente.setNome(nome.trim());
        }
        if (descricao != null) {
            existente.setDescricao(descricao.trim());
        }

        return categoriaDAO.atualizar(existente);
    }

    public boolean excluir(int id) {
        if (id <= 0) {
            return false;
        }
        return categoriaDAO.excluir(id);
    }
}
