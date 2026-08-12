package model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Representa a entidade Produto no sistema Dupla Cor (Catálogo).
 * Mapeamento da tabela: Produto
 */
public class Produto {

    private int idProduto;
    private String nome;
    private String marca;
    private BigDecimal precoBase;
    private String status;
    private List<Categoria> categorias;

    // Construtor vazio
    public Produto() {
        this.categorias = new ArrayList<>();
    }

    // Construtor com ID (leitura do banco)
    public Produto(int idProduto, String nome, String marca, BigDecimal precoBase, String status) {
        this.idProduto = idProduto;
        this.nome = nome;
        this.marca = marca;
        this.precoBase = precoBase;
        this.status = status;
        this.categorias = new ArrayList<>();
    }

    // Construtor para inserção (sem ID)
    public Produto(String nome, String marca, BigDecimal precoBase, String status) {
        this.nome = nome;
        this.marca = marca;
        this.precoBase = precoBase;
        this.status = status;
        this.categorias = new ArrayList<>();
    }

    // Getters e Setters

    public int getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(int idProduto) {
        this.idProduto = idProduto;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public BigDecimal getPrecoBase() {
        return precoBase;
    }

    public void setPrecoBase(BigDecimal precoBase) {
        this.precoBase = precoBase;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<Categoria> categorias) {
        this.categorias = (categorias != null) ? categorias : new ArrayList<>();
    }

    public void adicionarCategoria(Categoria categoria) {
        if (categoria != null && !this.categorias.contains(categoria)) {
            this.categorias.add(categoria);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Produto produto = (Produto) o;
        return idProduto == produto.idProduto;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idProduto);
    }

    @Override
    public String toString() {
        return "Produto{" +
                "idProduto=" + idProduto +
                ", nome='" + nome + '\'' +
                ", marca='" + marca + '\'' +
                ", precoBase=" + precoBase +
                ", status='" + status + '\'' +
                ", categorias=" + categorias.size() +
                '}';
    }
}