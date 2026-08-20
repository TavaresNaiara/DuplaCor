package controller;

import dao.*;
import model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller responsável pelo fluxo de vendas, criação de pedidos e execução do algoritmo FEFO na baixa de lotes.
 */
public class PedidoController {

    private final PedidoDAO pedidoDAO;
    private final ItemPedidoDAO itemPedidoDAO;
    private final LoteDAO loteDAO;
    private final ProdutoDAO produtoDAO;
    private final UsuarioDAO usuarioDAO;
    private final UsuarioCarrinhoDAO carrinhoDAO;

    public PedidoController() {
        this.pedidoDAO = new PedidoDAO();
        this.itemPedidoDAO = new ItemPedidoDAO();
        this.loteDAO = new LoteDAO();
        this.produtoDAO = new ProdutoDAO();
        this.usuarioDAO = new UsuarioDAO();
        this.carrinhoDAO = new UsuarioCarrinhoDAO();
    }

    public PedidoController(PedidoDAO pedidoDAO, ItemPedidoDAO itemPedidoDAO, LoteDAO loteDAO,
                            ProdutoDAO produtoDAO, UsuarioDAO usuarioDAO, UsuarioCarrinhoDAO carrinhoDAO) {
        this.pedidoDAO = pedidoDAO;
        this.itemPedidoDAO = itemPedidoDAO;
        this.loteDAO = loteDAO;
        this.produtoDAO = produtoDAO;
        this.usuarioDAO = usuarioDAO;
        this.carrinhoDAO = carrinhoDAO;
    }

    /**
     * Finaliza a compra a partir dos itens presentes no Carrinho Persistente do Usuário.
     * Aplica o algoritmo FEFO automaticamente para reserva e baixa nos lotes com menor prazo de validade.
     */
    public Pedido finalizarCompraDoCarrinho(int usuarioId, String statusPagamento) {
        Usuario usuario = usuarioDAO.buscarPorId(usuarioId);
        if (usuario == null) {
            System.err.println("Erro: Usuário de ID " + usuarioId + " não encontrado.");
            return null;
        }

        List<UsuarioCarrinho> itensCarrinho = carrinhoDAO.listarPorUsuario(usuarioId);
        if (itensCarrinho.isEmpty()) {
            System.err.println("Erro: O carrinho do usuário está vazio.");
            return null;
        }

        // Criar Pedido inicial com total zero
        Pedido pedido = new Pedido(0, LocalDateTime.now(), BigDecimal.ZERO,
                (statusPagamento != null ? statusPagamento.toUpperCase() : "APROVADO"), "NAO_INFORMADO", usuarioId);
        pedido.setUsuario(usuario);

        boolean inseriuPedido = pedidoDAO.inserir(pedido);
        if (!inseriuPedido) {
            System.err.println("Erro ao criar o Pedido.");
            return null;
        }

        BigDecimal totalPedido = BigDecimal.ZERO;
        List<ItemPedido> itensGerados = new ArrayList<>();

        for (UsuarioCarrinho itemCart : itensCarrinho) {
            int produtoId = itemCart.getProdutoId();
            int qtdDesejada = itemCart.getQuantidade();

            Produto produto = produtoDAO.buscarPorId(produtoId);
            if (produto == null) {
                continue;
            }

            // Obter lotes ordenados por validade mais próxima (FEFO)
            List<Lote> lotesFEFO = loteDAO.listarDisponiveisFEFO(produtoId);
            int qtdRestante = qtdDesejada;

            for (Lote lote : lotesFEFO) {
                if (qtdRestante <= 0) break;

                int saldoLote = lote.getQuantAtual();
                if (saldoLote <= 0) continue;

                int qtdAlocada = Math.min(qtdRestante, saldoLote);

                // Criar ItemPedido vinculado a este lote específico
                ItemPedido itemPedido = new ItemPedido(
                        0,
                        qtdAlocada,
                        produto.getPrecoBase(),
                        lote.getIdLote(),
                        pedido.getIdPedido()
                );
                itemPedido.setLote(lote);
                itemPedido.setPedido(pedido);

                boolean inseriuItem = itemPedidoDAO.inserir(itemPedido);
                if (inseriuItem) {
                    itensGerados.add(itemPedido);

                    // Baixa de estoque no Lote
                    int novoSaldo = saldoLote - qtdAlocada;
                    loteDAO.atualizarQuantidade(lote.getIdLote(), novoSaldo);

                    BigDecimal subtotal = produto.getPrecoBase().multiply(BigDecimal.valueOf(qtdAlocada));
                    totalPedido = totalPedido.add(subtotal);
                }

                qtdRestante -= qtdAlocada;
            }

            if (qtdRestante > 0) {
                System.out.println("Aviso: Para o produto '" + produto.getNome() +
                        "', " + qtdRestante + " unidade(s) não puderam ser atendidas por falta de lote disponível.");
            }
        }

        // Atualizar total consolidado do Pedido
        pedido.setTotal(totalPedido);
        pedido.setItensPedido(itensGerados);
        pedidoDAO.atualizar(pedido);

        // Limpar carrinho após finalização bem-sucedida
        carrinhoDAO.limparCarrinhoDoUsuario(usuarioId);

        return pedido;
    }

    /**
     * Realiza uma venda direta de um produto aplicando FEFO.
     */
    public Pedido realizarVendaDireta(int usuarioId, int produtoId, int quantidade, String statusPagamento) {
        if (quantidade <= 0) {
            System.err.println("Erro: A quantidade deve ser maior que zero.");
            return null;
        }

        Usuario usuario = usuarioDAO.buscarPorId(usuarioId);
        if (usuario == null) {
            System.err.println("Erro: Usuário não encontrado.");
            return null;
        }

        Produto produto = produtoDAO.buscarPorId(produtoId);
        if (produto == null) {
            System.err.println("Erro: Produto não encontrado.");
            return null;
        }

        List<Lote> lotesFEFO = loteDAO.listarDisponiveisFEFO(produtoId);
        int estoqueTotalDisponivel = lotesFEFO.stream().mapToInt(Lote::getQuantAtual).sum();
        if (estoqueTotalDisponivel < quantidade) {
            System.err.println("Erro: Estoque insuficiente. Disponível em lotes válidos: " + estoqueTotalDisponivel);
            return null;
        }

        Pedido pedido = new Pedido(0, LocalDateTime.now(), BigDecimal.ZERO,
                (statusPagamento != null ? statusPagamento.toUpperCase() : "APROVADO"), "NAO_INFORMADO", usuarioId);
        pedido.setUsuario(usuario);

        boolean inseriuPedido = pedidoDAO.inserir(pedido);
        if (!inseriuPedido) {
            return null;
        }

        BigDecimal totalPedido = BigDecimal.ZERO;
        List<ItemPedido> itensGerados = new ArrayList<>();
        int qtdRestante = quantidade;

        for (Lote lote : lotesFEFO) {
            if (qtdRestante <= 0) break;

            int saldoLote = lote.getQuantAtual();
            int qtdAlocada = Math.min(qtdRestante, saldoLote);

            ItemPedido itemPedido = new ItemPedido(
                    0,
                    qtdAlocada,
                    produto.getPrecoBase(),
                    lote.getIdLote(),
                    pedido.getIdPedido()
            );

            boolean inseriuItem = itemPedidoDAO.inserir(itemPedido);
            if (inseriuItem) {
                itensGerados.add(itemPedido);
                loteDAO.atualizarQuantidade(lote.getIdLote(), saldoLote - qtdAlocada);
                BigDecimal subtotal = produto.getPrecoBase().multiply(BigDecimal.valueOf(qtdAlocada));
                totalPedido = totalPedido.add(subtotal);
            }
            qtdRestante -= qtdAlocada;
        }

        pedido.setTotal(totalPedido);
        pedido.setItensPedido(itensGerados);
        pedidoDAO.atualizar(pedido);

        return pedido;
    }

    public Pedido buscarPorId(int id) {
        if (id <= 0) {
            return null;
        }
        Pedido pedido = pedidoDAO.buscarPorId(id);
        if (pedido != null) {
            pedido.setUsuario(usuarioDAO.buscarPorId(pedido.getUsuarioId()));
            List<ItemPedido> itens = itemPedidoDAO.listarPorPedido(id);
            for (ItemPedido item : itens) {
                Lote lote = loteDAO.buscarPorId(item.getLoteId());
                if (lote != null) {
                    lote.setProduto(produtoDAO.buscarPorId(lote.getProdutoId()));
                    item.setLote(lote);
                }
                item.setPedido(pedido);
            }
            pedido.setItensPedido(itens);
        }
        return pedido;
    }

    public List<Pedido> listarTodos() {
        List<Pedido> pedidos = pedidoDAO.listarTodos();
        for (Pedido p : pedidos) {
            p.setUsuario(usuarioDAO.buscarPorId(p.getUsuarioId()));
            List<ItemPedido> itens = itemPedidoDAO.listarPorPedido(p.getIdPedido());
            for (ItemPedido item : itens) {
                Lote lote = loteDAO.buscarPorId(item.getLoteId());
                if (lote != null) {
                    lote.setProduto(produtoDAO.buscarPorId(lote.getProdutoId()));
                    item.setLote(lote);
                }
                item.setPedido(p);
            }
            p.setItensPedido(itens);
        }
        return pedidos;
    }

    public List<Pedido> listarPorUsuario(int usuarioId) {
        List<Pedido> pedidos = pedidoDAO.listarPorUsuario(usuarioId);
        Usuario user = usuarioDAO.buscarPorId(usuarioId);
        for (Pedido p : pedidos) {
            p.setUsuario(user);
            List<ItemPedido> itens = itemPedidoDAO.listarPorPedido(p.getIdPedido());
            for (ItemPedido item : itens) {
                Lote lote = loteDAO.buscarPorId(item.getLoteId());
                if (lote != null) {
                    lote.setProduto(produtoDAO.buscarPorId(lote.getProdutoId()));
                    item.setLote(lote);
                }
                item.setPedido(p);
            }
            p.setItensPedido(itens);
        }
        return pedidos;
    }

    public boolean atualizarStatusPagamento(int idPedido, String novoStatus) {
        if (idPedido <= 0 || novoStatus == null) {
            return false;
        }
        return pedidoDAO.atualizarStatusPagamento(idPedido, novoStatus.trim().toUpperCase());
    }

    public boolean excluir(int id) {
        if (id <= 0) {
            return false;
        }
        itemPedidoDAO.excluirPorPedido(id);
        return pedidoDAO.excluir(id);
    }
}
