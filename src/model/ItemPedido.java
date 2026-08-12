package model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Representa a entidade ItemPedido no sistema Dupla Cor (Itens de um pedido vinculados ao lote específico).
 * Mapeamento da tabela: ItemPedido
 */
public class ItemPedido {

    private int idItemPedido;
    private int quantidade;
    private BigDecimal precoAplicado;
    private int loteId;
    private Lote lote;
    private int pedidoId;
    private Pedido pedido;

    // Construtor vazio
    public ItemPedido() {
    }

    // Construtor completo com IDs (leitura do banco)
    public ItemPedido(int idItemPedido, int quantidade, BigDecimal precoAplicado, int loteId, int pedidoId) {
        this.idItemPedido = idItemPedido;
        this.quantidade = quantidade;
        this.precoAplicado = precoAplicado;
        this.loteId = loteId;
        this.pedidoId = pedidoId;
    }

    // Construtor completo com Objetos
    public ItemPedido(int idItemPedido, int quantidade, BigDecimal precoAplicado, Lote lote, Pedido pedido) {
        this.idItemPedido = idItemPedido;
        this.quantidade = quantidade;
        this.precoAplicado = precoAplicado;
        this.lote = lote;
        this.pedido = pedido;
        if (lote != null) {
            this.loteId = lote.getIdLote();
        }
        if (pedido != null) {
            this.pedidoId = pedido.getIdPedido();
        }
    }

    // Construtor para inserção (sem idItemPedido)
    public ItemPedido(int quantidade, BigDecimal precoAplicado, Lote lote, Pedido pedido) {
        this.quantidade = quantidade;
        this.precoAplicado = precoAplicado;
        this.lote = lote;
        this.pedido = pedido;
        if (lote != null) {
            this.loteId = lote.getIdLote();
        }
        if (pedido != null) {
            this.pedidoId = pedido.getIdPedido();
        }
    }

    // Getters e Setters

    public int getIdItemPedido() {
        return idItemPedido;
    }

    public void setIdItemPedido(int idItemPedido) {
        this.idItemPedido = idItemPedido;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPrecoAplicado() {
        return precoAplicado;
    }

    public void setPrecoAplicado(BigDecimal precoAplicado) {
        this.precoAplicado = precoAplicado;
    }

    public int getLoteId() {
        if (lote != null) {
            return lote.getIdLote();
        }
        return loteId;
    }

    public void setLoteId(int loteId) {
        this.loteId = loteId;
    }

    public Lote getLote() {
        return lote;
    }

    public void setLote(Lote lote) {
        this.lote = lote;
        if (lote != null) {
            this.loteId = lote.getIdLote();
        }
    }

    public int getPedidoId() {
        if (pedido != null) {
            return pedido.getIdPedido();
        }
        return pedidoId;
    }

    public void setPedidoId(int pedidoId) {
        this.pedidoId = pedidoId;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
        if (pedido != null) {
            this.pedidoId = pedido.getIdPedido();
        }
    }

    /**
     * Calcula o subtotal do item
     */
    public BigDecimal getSubtotal() {
        if (precoAplicado != null && quantidade > 0) {
            return precoAplicado.multiply(BigDecimal.valueOf(quantidade));
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPedido that = (ItemPedido) o;
        return idItemPedido == that.idItemPedido && pedidoId == that.pedidoId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idItemPedido, pedidoId);
    }

    @Override
    public String toString() {
        return "ItemPedido{" +
                "idItemPedido=" + idItemPedido +
                ", quantidade=" + quantidade +
                ", precoAplicado=" + precoAplicado +
                ", loteId=" + getLoteId() +
                ", pedidoId=" + getPedidoId() +
                ", subtotal=" + getSubtotal() +
                '}';
    }
}
