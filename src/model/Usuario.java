package model;

import java.time.LocalDateTime;

public class Usuario {

    private int 		idUsuario;
    private String 		nome;
    private String 		email;
    private String 		senha;
    private String 		perfil;
    private LocalDateTime dataCadastro;
    private String tokenRecuperacao;

    
    public Usuario() {
    }

    // Construtor com todos os atributos
    public Usuario(int idUsuario, String nome, String email, String senha,
                   String perfil, LocalDateTime dataCadastro,
                   String tokenRecuperacao) {
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.perfil = perfil;
        this.dataCadastro = dataCadastro;
        this.tokenRecuperacao = tokenRecuperacao;
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
    public String toString() {
        return "Usuario{" +
                "idUsuario=" + idUsuario +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", perfil='" + perfil + '\'' +
                ", dataCadastro=" + dataCadastro +
                '}';
    }
}