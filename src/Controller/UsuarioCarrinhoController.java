package controller;

import dao.ProdutoDAO;
import dao.UsuarioCarrinhoDAO;
import dao.UsuarioDAO;
import model.Produto;
import model.Usuario;
import model.UsuarioCarrinho;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller responsável pelo gerenciamento do Carrinho Persistente do Usuário.
 */
public class UsuarioCarrinhoController {

    private final UsuarioCarrinhoDAO carrinhoDAO;
    private final ProdutoDAO produtoDAO;
    private final UsuarioDAO usuarioDAO;

    public UsuarioCarrinhoController() {
        this.carrinhoDAO = new UsuarioCarrinhoDAO();
        this.produtoDAO = new ProdutoDAO();
        this.usuarioDAO = new UsuarioDAO();
    }

    public UsuarioCarrinhoController(UsuarioCarrinhoDAO carrinhoDAO, ProdutoDAO produtoDAO, UsuarioDAO usuarioDAO) {
        this.carrinhoDAO = carrinhoDAO;
        this.produtoDAO = produtoDAO;
        this.usuarioDAO = usuarioDAO;
    }

    public boolean adicionarItem(int usuarioId, int produtoId, int quantidade) {
        if (quantidade <= 0) {
            System.err.println("Erro de validação: A quantidade deve ser maior que zero.");
            return false;
        }

        Usuario usuario = usuarioDAO.buscarPorId(usuarioId);
        if (usuario == null) {
            System.err.println("Erro: Usuário não encontrado.");
            return false;
        }

        Produto produto = produtoDAO.buscarPorId(produtoId);
        if (produto == null) {
            System.err.println("Erro: Produto não encontrado.");
            return false;
        }

        return carrinhoDAO.adicionarOuIncrementar(usuarioId, produtoId, quantidade);
    }

    public List<UsuarioCarrinho> listarPorUsuario(int usuarioId) {
        List<UsuarioCarrinho> itens = carrinhoDAO.listarPorUsuario(usuarioId);
        Usuario user = usuarioDAO.buscarPorId(usuarioId);
        for (UsuarioCarrinho item : itens) {
            item.setUsuario(user);
            item.setProduto(produtoDAO.buscarPorId(item.getProdutoId()));
        }
        return itens;
    }

    public BigDecimal calcularTotalCarrinho(int usuarioId) {
        List<UsuarioCarrinho> itens = listarPorUsuario(usuarioId);
        BigDecimal total = BigDecimal.ZERO;
        for (UsuarioCarrinho item : itens) {
            if (item.getProduto() != null && item.getProduto().getPrecoBase() != null) {
                BigDecimal subtotal = item.getProduto().getPrecoBase().multiply(BigDecimal.valueOf(item.getQuantidade()));
                total = total.add(subtotal);
            }
        }
        return total;
    }

    public boolean atualizarQuantidade(int idUsuarioCarrinho, int novaQuantidade) {
        if (idUsuarioCarrinho <= 0) {
            return false;
        }
        if (novaQuantidade <= 0) {
            return carrinhoDAO.excluir(idUsuarioCarrinho);
        }
        return carrinhoDAO.atualizarQuantidade(idUsuarioCarrinho, novaQuantidade);
    }

    public boolean removerItem(int idUsuarioCarrinho) {
        if (idUsuarioCarrinho <= 0) {
            return false;
        }
        return carrinhoDAO.excluir(idUsuarioCarrinho);
    }

    public boolean limparCarrinho(int usuarioId) {
        if (usuarioId <= 0) {
            return false;
        }
        return carrinhoDAO.limparCarrinhoDoUsuario(usuarioId);
    }
}
