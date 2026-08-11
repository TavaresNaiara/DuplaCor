package model;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.Objects;


public class Perda {

	private Integer 	idPerda;
	private Produto 	produto;
	private Integer 	quantidade;
	private LocalDate 	dataVenda;
	private LocalDate 	dataRegistro;
	private BigDecimal 	precoCusto;
	private BigDecimal 	valorPerda;
	private String 		motivo;
	private String 		observacoes;

	/**
	 * Construtor vazio da classe Perda
	 */
	public Perda() {
	}

	
	public Perda(Integer idPerda, Produto produto, Integer quantidade, LocalDate dataVenda, LocalDate dataRegistro,
			BigDecimal precoCusto, BigDecimal valorPerda, String motivo, String observacoes) {
		this.idPerda = idPerda;
		this.produto = produto;
		this.quantidade = quantidade;
		this.dataVenda = dataVenda;
		this.dataRegistro = dataRegistro;
		this.precoCusto = precoCusto;
		this.valorPerda = valorPerda;
		this.motivo = motivo;
		this.observacoes = observacoes;
	}

	/**
	 * Construtor alternativo sem ID (para inserção em banco)
	 */
	public Perda(Produto produto, Integer quantidade, LocalDate dataVenda, BigDecimal precoCusto, String motivo) {
		this.produto = produto;
		this.quantidade = quantidade;
		this.dataVenda = dataVenda;
		this.precoCusto = precoCusto;
		this.motivo = motivo;
		this.dataRegistro = LocalDate.now();
		calcularValorPerda();
	}

	//  GETTERS E SETTERS 
	
	public Integer getIdPerda() {
		return idPerda;
	}

	public void setIdPerda(Integer idPerda) {
		this.idPerda = idPerda;
	}

	public Produto getProduto() {
		return produto;
	}

	public void setProduto(Produto produto) {
		this.produto = produto;
	}

	public Integer getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(Integer quantidade) {
		this.quantidade = quantidade;
	}

	public LocalDate getDataVenda() {
		return dataVenda;
	}

	public void setDataVenda(LocalDate dataVenda) {
		this.dataVenda = dataVenda;
	}

	public LocalDate getDataRegistro() {
		return dataRegistro;
	}

	public void setDataRegistro(LocalDate dataRegistro) {
		this.dataRegistro = dataRegistro;
	}

	public BigDecimal getPrecoCusto() {
		return precoCusto;
	}

	public void setPrecoCusto(BigDecimal precoCusto) {
		this.precoCusto = precoCusto;
	}

	public BigDecimal getValorPerda() {
		return valorPerda;
	}

	public void setValorPerda(BigDecimal valorPerda) {
		this.valorPerda = valorPerda;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	public String getObservacoes() {
		return observacoes;
	}

	public void setObservacoes(String observacoes) {
		this.observacoes = observacoes;
	}

	/**
	 * Método auxiliar para calcular o valor total da perda automaticamente
	 * Valor da perda = quantidade * preço de custo
	 */
	public void calcularValorPerda() {
		if (this.quantidade != null && this.precoCusto != null) {
			this.valorPerda = this.precoCusto.multiply(new BigDecimal(this.quantidade));
		}
	}

	/**
	 * Método auxiliar para obter o número de dias entre a venda e o registro da perda
	 */
	public long diasAtePerda() {
		if (this.dataVenda != null && this.dataRegistro != null) {
			return java.time.temporal.ChronoUnit.DAYS.between(this.dataVenda, this.dataRegistro);
		}
		return 0;
	}

	//  EQUALS E HASHCODE

	@Override
	public int hashCode() {
		return Objects.hash(idPerda);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Perda other = (Perda) obj;
		return Objects.equals(idPerda, other.idPerda);
	}

	//  TO STRING 

	@Override
	public String toString() {
		return "Perda [idPerda=" + idPerda + ", produto=" + (produto != null ? produto.getNome() : "null")
				+ ", quantidade=" + quantidade + ", dataVenda=" + dataVenda + ", dataRegistro=" + dataRegistro
				+ ", precoCusto=" + precoCusto + ", valorPerda=" + valorPerda + ", motivo=" + motivo + "]";
	}
}