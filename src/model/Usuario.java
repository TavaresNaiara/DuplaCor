package model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa a entidade Usuario no sistema Dupla Cor.
 * Mapeamento da tabela: Usuario
 */
public class Usuario {

    private int idUsuario;
    private String nome;
    private String email;
    private String senha;
    private String perfil;
    private LocalDateTime dataCadastro;
    private String tokenRecuperacao;

    // Construtor vazio
    public Usuario() {
    }

    // Construtor com ID (leitura do banco)
    public Usuario(int idUsuario, String nome, String email, String senha,
                   String perfil, LocalDateTime dataCadastro, String tokenRecuperacao) {
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.perfil = perfil;
        this.dataCadastro = dataCadastro;
        this.tokenRecuperacao = tokenRecuperacao;
    }

    // Construtor para cadastro inicial
    public Usuario(String nome, String email, String senha, String perfil) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.perfil = perfil;
        this.dataCadastro = LocalDateTime.now();
    }

    // Getters e Setters

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public String getTokenRecuperacao() {
        return tokenRecuperacao;
    }

    public void setTokenRecuperacao(String tokenRecuperacao) {
        this.tokenRecuperacao = tokenRecuperacao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return idUsuario == usuario.idUsuario;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUsuario);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "idUsuario=" + idUsuario +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", perfil='" + perfil + '\'' +
                ", dataCadastro=" + dataCadastro +
                ", tokenRecuperacao='" + tokenRecuperacao + '\'' +
                '}';
    }
}