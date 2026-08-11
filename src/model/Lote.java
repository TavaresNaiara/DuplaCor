package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Lote {

    private int idLote;
    private String numeroLote;
    private LocalDate dataFabricacao;
    private LocalDate dataValidade;
    private int quantidade;
    private BigDecimal precoCompra;
    private BigDecimal precoVenda;
    private Produto produto;

   
    public Lote() {
    }

    
    public Lote(int idLote, String numeroLote, LocalDate dataFabricacao,
                LocalDate dataValidade, int quantidade,
                BigDecimal precoCompra, BigDecimal precoVenda,
                Produto produto) {
        this.idLote = idLote;
        this.numeroLote = numeroLote;
        this.dataFabricacao = dataFabricacao;
        this.dataValidade = dataValidade;
        this.quantidade = quantidade;
        this.precoCompra = precoCompra;
        this.precoVenda = precoVenda;
        this.produto = produto;
    }

    public int getIdLote() {
        return idLote;
    }

    public void setIdLote(int idLote) {
        this.idLote = idLote;
    }

    public String getNumeroLote() {
        return numeroLote;
    }

    public void setNumeroLote(String numeroLote) {
        this.numeroLote = numeroLote;
    }

    public LocalDate getDataFabricacao() {
        return dataFabricacao;
    }

    public void setDataFabricacao(LocalDate dataFabricacao) {
        this.dataFabricacao = dataFabricacao;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(LocalDate dataValidade) {
        this.dataValidade = dataValidade;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPrecoCompra() {
        return precoCompra;
    }

    public void setPrecoCompra(BigDecimal precoCompra) {
        this.precoCompra = precoCompra;
    }

    public BigDecimal getPrecoVenda() {
        return precoVenda;
    }

    public void setPrecoVenda(BigDecimal precoVenda) {
        this.precoVenda = precoVenda;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    @Override
    public String toString() {
        return "Lote{" +
                "idLote=" + idLote +
                ", numeroLote='" + numeroLote + '\'' +
                ", dataFabricacao=" + dataFabricacao +
                ", dataValidade=" + dataValidade +
                ", quantidade=" + quantidade +
                ", precoCompra=" + precoCompra +
                ", precoVenda=" + precoVenda +
                ", produto=" + produto.getNome() +
                '}';
    }
}