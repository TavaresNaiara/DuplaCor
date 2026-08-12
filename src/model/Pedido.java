package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Representa a entidade Pedido no sistema Dupla Cor (Registro de vendas).
 * Mapeamento da tabela: Pedido
 */
public class Pedido {

    private int idPedido;
    private LocalDateTime dataVenda;
    private BigDecimal total;
    private String statusPagamento;
    private int usuarioId;
    private Usuario usuario;
    private List<ItemPedido> itensPedido;

    // Construtor vazio
    public Pedido() {
        this.itensPedido = new ArrayList<>();
        this.total = BigDecimal.ZERO;
    }

    // Construtor com ID (leitura do banco)
    public Pedido(int idPedido, LocalDateTime dataVenda, BigDecimal total,
                  String statusPagamento, int usuarioId) {
        this.idPedido = idPedido;
        this.dataVenda = dataVenda;
        this.total = total;
        this.statusPagamento = statusPagamento;
        this.usuarioId = usuarioId;
        this.itensPedido = new ArrayList<>();
    }

    // Construtor completo com Usuario objeto
    public Pedido(int idPedido, LocalDateTime dataVenda, BigDecimal total,
                  String statusPagamento, Usuario usuario) {
        this.idPedido = idPedido;
        this.dataVenda = dataVenda;
        this.total = total;
        this.statusPagamento = statusPagamento;
        this.usuario = usuario;
        if (usuario != null) {
            this.usuarioId = usuario.getIdUsuario();
        }
        this.itensPedido = new ArrayList<>();
    }

    // Construtor para inserção (sem ID)
    public Pedido(BigDecimal total, String statusPagamento, Usuario usuario) {
        this.dataVenda = LocalDateTime.now();
        this.total = total;
        this.statusPagamento = statusPagamento;
        this.usuario = usuario;
        if (usuario != null) {
            this.usuarioId = usuario.getIdUsuario();
        }
        this.itensPedido = new ArrayList<>();
    }

    // Getters e Setters

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public LocalDateTime getDataVenda() {
        return dataVenda;
    }

    public void setDataVenda(LocalDateTime dataVenda) {
        this.dataVenda = dataVenda;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getStatusPagamento() {
        return statusPagamento;
    }

    public void setStatusPagamento(String statusPagamento) {
        this.statusPagamento = statusPagamento;
    }

    public int getUsuarioId() {
        if (usuario != null) {
            return usuario.getIdUsuario();
        }
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        if (usuario != null) {
            this.usuarioId = usuario.getIdUsuario();
        }
    }

    public List<ItemPedido> getItensPedido() {
        return itensPedido;
    }

    public void setItensPedido(List<ItemPedido> itensPedido) {
        this.itensPedido = (itensPedido != null) ? itensPedido : new ArrayList<>();
    }

    public void adicionarItem(ItemPedido item) {
        if (item != null) {
            this.itensPedido.add(item);
            recalcularTotal();
        }
    }

    public void recalcularTotal() {
        BigDecimal soma = BigDecimal.ZERO;
        for (ItemPedido item : itensPedido) {
            if (item.getPrecoAplicado() != null && item.getQuantidade() > 0) {
                BigDecimal subtotal = item.getPrecoAplicado().multiply(BigDecimal.valueOf(item.getQuantidade()));
                soma = soma.add(subtotal);
            }
        }
        this.total = soma;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pedido pedido = (Pedido) o;
        return idPedido == pedido.idPedido;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPedido);
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "idPedido=" + idPedido +
                ", dataVenda=" + dataVenda +
                ", total=" + total +
                ", statusPagamento='" + statusPagamento + '\'' +
                ", usuarioId=" + getUsuarioId() +
                (usuario != null ? ", usuario='" + usuario.getNome() + '\'' : "") +
                ", itens=" + itensPedido.size() +
                '}';
    }
}