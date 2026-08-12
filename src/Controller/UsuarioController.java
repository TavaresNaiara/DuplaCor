package controller;

import dao.UsuarioDAO;
import model.Usuario;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Controller responsável pelas regras de negócio de Usuários.
 */
public class UsuarioController {

    private final UsuarioDAO usuarioDAO;

    public UsuarioController() {
        this.usuarioDAO = new UsuarioDAO();
    }

    public UsuarioController(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public boolean cadastrar(String nome, String email, String senha, String perfil) {
        if (nome == null || nome.trim().isEmpty()) {
            System.err.println("Erro de validação: O nome do usuário não pode ser vazio.");
            return false;
        }
        if (email == null || !email.contains("@")) {
            System.err.println("Erro de validação: E-mail inválido.");
            return false;
        }
        if (senha == null || senha.trim().length() < 4) {
            System.err.println("Erro de validação: A senha deve conter pelo menos 4 caracteres.");
            return false;
        }

        Usuario existente = usuarioDAO.buscarPorEmail(email.trim());
        if (existente != null) {
            System.err.println("Erro: Já existe um usuário cadastrado com este e-mail.");
            return false;
        }

        Usuario novo = new Usuario(
                0,
                nome.trim(),
                email.trim(),
                senha.trim(),
                (perfil != null && !perfil.trim().isEmpty()) ? perfil.trim().toUpperCase() : "CLIENTE",
                LocalDateTime.now(),
                null
        );

        return usuarioDAO.inserir(novo);
    }

    public Usuario autenticar(String email, String senha) {
        if (email == null || senha == null) {
            return null;
        }
        return usuarioDAO.autenticar(email.trim(), senha.trim());
    }

    public Usuario buscarPorId(int id) {
        if (id <= 0) {
            return null;
        }
        return usuarioDAO.buscarPorId(id);
    }

    public Usuario buscarPorEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        return usuarioDAO.buscarPorEmail(email.trim());
    }

    public List<Usuario> listarTodos() {
        return usuarioDAO.listarTodos();
    }

    public boolean atualizar(int id, String nome, String email, String senha, String perfil) {
        Usuario existente = usuarioDAO.buscarPorId(id);
        if (existente == null) {
            System.err.println("Erro: Usuário com ID " + id + " não encontrado.");
            return false;
        }

        if (nome != null && !nome.trim().isEmpty()) {
            existente.setNome(nome.trim());
        }
        if (email != null && email.contains("@")) {
            existente.setEmail(email.trim());
        }
        if (senha != null && senha.trim().length() >= 4) {
            existente.setSenha(senha.trim());
        }
        if (perfil != null && !perfil.trim().isEmpty()) {
            existente.setPerfil(perfil.trim().toUpperCase());
        }

        return usuarioDAO.atualizar(existente);
    }

    /**
     * Regra de negócio do escopo: gera um token/nova senha aleatória para envio
     */
    public String recuperarSenha(String email) {
        Usuario user = usuarioDAO.buscarPorEmail(email);
        if (user == null) {
            System.err.println("Erro: Nenhum usuário encontrado com o e-mail " + email);
            return null;
        }
        String novaSenha = UUID.randomUUID().toString().substring(0, 8);
        user.setSenha(novaSenha);
        user.setTokenRecuperacao(UUID.randomUUID().toString().substring(0, 16));
        boolean atualizou = usuarioDAO.atualizar(user);
        if (atualizou) {
            return novaSenha;
        }
        return null;
    }

    public boolean excluir(int id) {
        if (id <= 0) {
            return false;
        }
        return usuarioDAO.excluir(id);
    }
}
