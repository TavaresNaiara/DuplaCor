package model;

import java.util.Objects;

/**
 * Representa o relacionamento N:N entre Produto e Categoria.
 * Mapeamento da tabela: Produto_has_Categoria
 */
public class ProdutoCategoria {

    private int produtoId;
    private int categoriaId;
    private Produto produto;
    private Categoria categoria;

    // Construtor vazio
    public ProdutoCategoria() {
    }

    // Construtor com IDs
    public ProdutoCategoria(int produtoId, int categoriaId) {
        this.produtoId = produtoId;
        this.categoriaId = categoriaId;
    }

    // Construtor completo com entidades
    public ProdutoCategoria(Produto produto, Categoria categoria) {
        this.produto = produto;
        this.categoria = categoria;
        if (produto != null) {
            this.produtoId = produto.getIdProduto();
        }
        if (categoria != null) {
            this.categoriaId = categoria.getIdCategoria();
        }
    }

    // Getters e Setters

    public int getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(int produtoId) {
        this.produtoId = produtoId;
    }

    public int getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(int categoriaId) {
        this.categoriaId = categoriaId;
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

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
        if (categoria != null) {
            this.categoriaId = categoria.getIdCategoria();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProdutoCategoria that = (ProdutoCategoria) o;
        return produtoId == that.produtoId && categoriaId == that.categoriaId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(produtoId, categoriaId);
    }

    @Override
    public String toString() {
        return "ProdutoCategoria{" +
                "produtoId=" + produtoId +
                ", categoriaId=" + categoriaId +
                '}';
    }
}
