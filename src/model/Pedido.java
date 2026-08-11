package model;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Pedido {

	private Integer idPedido;
	private LocalDateTime dataVenda;
	private BigDecimal total;
	private String statusPagamento;
	private Usuario usuario;
	private List<ItemPedido> itensPedido;

	
	public Pedido() {
		this.itensPedido = new ArrayList<>();
	}

	
	public Pedido(Integer idPedido, LocalDateTime dataVenda, BigDecimal total, String statusPagamento,
			Usuario usuario) {
		this.idPedido = idPedido;
		this.dataVenda = dataVenda;
		this.total = total;
		this.statusPagamento = statusPagamento;
		this.usuario = usuario;
		this.itensPedido = new ArrayList<>();
	}

	
	public Pedido(Integer idPedido, LocalDateTime dataVenda, BigDecimal total, String statusPagamento,
			Usuario usuario, List<ItemPedido> itensPedido) {
		this.idPedido = idPedido;
		this.dataVenda = dataVenda;
		this.total = total;
		this.statusPagamento = statusPagamento;
		this.usuario = usuario;
		this.itensPedido = itensPedido != null ? itensPedido : new ArrayList<>();
	}

	//  GETTERS E SETTERS 

	public Integer getIdPedido() {
		return idPedido;
	}

	public void setIdPedido(Integer idPedido) {
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

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public List<ItemPedido> getItensPedido() {
		return itensPedido;
	}

	public void setItensPedido(List<ItemPedido> itensPedido) {
		this.itensPedido = itensPedido;
	}

	
	public void adicionarItem(ItemPedido item) {
		if (item != null) {
			this.itensPedido.add(item);
		}
	}

	
	public void removerItem(ItemPedido item) {
		if (item != null) {
			this.itensPedido.remove(item);
		}
	}

	// EQUALS E HASHCODE 

	@Override
	public int hashCode() {
		return Objects.hash(idPedido);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pedido other = (Pedido) obj;
		return Objects.equals(idPedido, other.idPedido);
	}

	//  TO STRING

	@Override
	public String toString() {
		return "Pedido [idPedido=" + idPedido + ", dataVenda=" + dataVenda + ", total=" + total
				+ ", statusPagamento=" + statusPagamento + ", usuario=" + usuario.getNome() + ", quantidadeItens="
				+ itensPedido.size() + "]";
	}
}