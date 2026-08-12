package model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Representa a entidade Lote no sistema Dupla Cor (Gestão de Estoque por Lote / FEFO).
 * Mapeamento da tabela: Lote
 */
public class Lote {

    private int idLote;
    private int quantInicial;
    private int quantAtual;
    private LocalDate dataValidade;
    private LocalDate dataEntrada;
    private String status;
    private int produtoId;
    private Produto produto;

    // Construtor vazio
    public Lote() {
    }

    // Construtor com ID (leitura do banco)
    public Lote(int idLote, int quantInicial, int quantAtual, LocalDate dataValidade,
                LocalDate dataEntrada, String status, int produtoId) {
        this.idLote = idLote;
        this.quantInicial = quantInicial;
        this.quantAtual = quantAtual;
        this.dataValidade = dataValidade;
        this.dataEntrada = dataEntrada;
        this.status = status;
        this.produtoId = produtoId;
    }

    // Construtor completo com Produto objeto
    public Lote(int idLote, int quantInicial, int quantAtual, LocalDate dataValidade,
                LocalDate dataEntrada, String status, Produto produto) {
        this.idLote = idLote;
        this.quantInicial = quantInicial;
        this.quantAtual = quantAtual;
        this.dataValidade = dataValidade;
        this.dataEntrada = dataEntrada;
        this.status = status;
        this.produto = produto;
        if (produto != null) {
            this.produtoId = produto.getIdProduto();
        }
    }

    // Construtor para inserção (sem ID)
    public Lote(int quantInicial, int quantAtual, LocalDate dataValidade,
                LocalDate dataEntrada, String status, Produto produto) {
        this.quantInicial = quantInicial;
        this.quantAtual = quantAtual;
        this.dataValidade = dataValidade;
        this.dataEntrada = dataEntrada;
        this.status = status;
        this.produto = produto;
        if (produto != null) {
            this.produtoId = produto.getIdProduto();
        }
    }

    // Getters e Setters

    public int getIdLote() {
        return idLote;
    }

    public void setIdLote(int idLote) {
        this.idLote = idLote;
    }

    public int getQuantInicial() {
        return quantInicial;
    }

    public void setQuantInicial(int quantInicial) {
        this.quantInicial = quantInicial;
    }

    public int getQuantAtual() {
        return quantAtual;
    }

    public void setQuantAtual(int quantAtual) {
        this.quantAtual = quantAtual;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(LocalDate dataValidade) {
        this.dataValidade = dataValidade;
    }

    public LocalDate getDataEntrada() {
        return dataEntrada;
    }

    public void setDataEntrada(LocalDate dataEntrada) {
        this.dataEntrada = dataEntrada;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProdutoId() {
        if (produto != null) {
            return produto.getIdProduto();
        }
        return produtoId;
    }

    public void setProdutoId(int produtoId) {
        this.produtoId = produtoId;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
        if (produto != null) {
            this.produtoId = produto.getIdProduto();
        }
    }

    /**
     * Verifica se o lote já ultrapassou a data de validade
     */
    public boolean isVencido() {
        return dataValidade != null && dataValidade.isBefore(LocalDate.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lote lote = (Lote) o;
        return idLote == lote.idLote;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idLote);
    }

    @Override
    public String toString() {
        return "Lote{" +
                "idLote=" + idLote +
                ", quantInicial=" + quantInicial +
                ", quantAtual=" + quantAtual +
                ", dataValidade=" + dataValidade +
                ", dataEntrada=" + dataEntrada +
                ", status='" + status + '\'' +
                ", produtoId=" + getProdutoId() +
                (produto != null ? ", produto='" + produto.getNome() + '\'' : "") +
                '}';
    }
}