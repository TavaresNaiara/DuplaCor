package model;

import java.math.BigDecimal;

public class Produto {

    private int 		idProduto;
    private String 		nome;
    private String 		marca;
    private BigDecimal 	precoBase;
    private String 		status;

    // Construtor vazio
    public Produto() {
    }

    // Construtor com parâmetros
    public Produto(int idProduto, String nome, String marca,
                   BigDecimal precoBase, String status) {
        this.idProduto = idProduto;
        this.nome = nome;
        this.marca = marca;
        this.precoBase = precoBase;
        this.status = status;
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

    @Override
    public String toString() {
        return "Produto{" +
                "idProduto=" + idProduto +
                ", nome='" + nome + '\'' +
                ", marca='" + marca + '\'' +
                ", precoBase=" + precoBase +
                ", status='" + status + '\'' +
                '}';
    }
}