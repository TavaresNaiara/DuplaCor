package model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa a entidade UsuarioCarrinho no sistema Dupla Cor (Carrinho Persistente).
 * Mapeamento da tabela: UsuarioCarrinho
 */
public class UsuarioCarrinho {

    private int idUsuarioCarrinho;
    private LocalDateTime dataAdicao;
    private int quantidade;
    private int usuarioId;
    private Usuario usuario;
    private int produtoId;
    private Produto produto;

    // Construtor vazio
    public UsuarioCarrinho() {
    }

    // Construtor completo com IDs (leitura do banco)
    public UsuarioCarrinho(int idUsuarioCarrinho, LocalDateTime dataAdicao,
                           int quantidade, int usuarioId, int produtoId) {
        this.idUsuarioCarrinho = idUsuarioCarrinho;
        this.dataAdicao = dataAdicao;
        this.quantidade = quantidade;
        this.usuarioId = usuarioId;
        this.produtoId = produtoId;
    }

    // Construtor com Objetos
    public UsuarioCarrinho(int idUsuarioCarrinho, LocalDateTime dataAdicao,
                           int quantidade, Usuario usuario, Produto produto) {
        this.idUsuarioCarrinho = idUsuarioCarrinho;
        this.dataAdicao = dataAdicao;
        this.quantidade = quantidade;
        this.usuario = usuario;
        this.produto = produto;
        if (usuario != null) {
            this.usuarioId = usuario.getIdUsuario();
        }
        if (produto != null) {
            this.produtoId = produto.getIdProduto();
        }
    }

    // Construtor para inserção (sem ID)
    public UsuarioCarrinho(int quantidade, Usuario usuario, Produto produto) {
        this.dataAdicao = LocalDateTime.now();
        this.quantidade = quantidade;
        this.usuario = usuario;
        this.produto = produto;
        if (usuario != null) {
            this.usuarioId = usuario.getIdUsuario();
        }
        if (produto != null) {
            this.produtoId = produto.getIdProduto();
        }
    }

    // Getters e Setters

    public int getIdUsuarioCarrinho() {
        return idUsuarioCarrinho;
    }

    public void setIdUsuarioCarrinho(int idUsuarioCarrinho) {
        this.idUsuarioCarrinho = idUsuarioCarrinho;
    }

    public LocalDateTime getDataAdicao() {
        return dataAdicao;
    }

    public void setDataAdicao(LocalDateTime dataAdicao) {
        this.dataAdicao = dataAdicao;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioCarrinho that = (UsuarioCarrinho) o;
        return idUsuarioCarrinho == that.idUsuarioCarrinho;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUsuarioCarrinho);
    }

    @Override
    public String toString() {
        return "UsuarioCarrinho{" +
                "idUsuarioCarrinho=" + idUsuarioCarrinho +
                ", dataAdicao=" + dataAdicao +
                ", quantidade=" + quantidade +
                ", usuarioId=" + getUsuarioId() +
                ", produtoId=" + getProdutoId() +
                (produto != null ? ", produto='" + produto.getNome() + '\'' : "") +
                '}';
    }
}
