package br.com.farmacia.service;

import br.com.farmacia.dao.PersistenciaException;
import br.com.farmacia.dao.UsuarioDAO;
import br.com.farmacia.model.Usuario;
import br.com.farmacia.security.SenhaHash;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public final class UsuarioService {
    private final UsuarioDAO dao;
    // Mesmo custo criptográfico quando o usuário não existe.
    private final String hashAusente = SenhaHash.gerar("credencial-inexistente".toCharArray());
    public UsuarioService(UsuarioDAO dao) { this.dao = dao; }

    public Optional<Usuario> autenticar(String login, char[] senha) {
        if (senha == null) throw new RegraNegocioException("Informe a senha.");
        try {
            String normalized = validarLogin(login);
            if (senha.length == 0 || senha.length > 128) {
                throw new RegraNegocioException("Informe uma senha com até 128 caracteres.");
            }
            Optional<UsuarioDAO.Credencial> credential = dao.buscarPorLogin(normalized);
            boolean valid = SenhaHash.verificar(senha,
                    credential.map(UsuarioDAO.Credencial::senhaHash).orElse(hashAusente));
            return valid ? credential.map(UsuarioDAO.Credencial::usuario) : Optional.empty();
        } finally { Arrays.fill(senha, '\0'); }
    }

    public Usuario cadastrar(String login, char[] senha, char[] confirmacao) {
        try {
            String normalized = validarLogin(login);
            if (senha == null || senha.length < 8 || senha.length > 128
                    || somenteEspacos(senha)) {
                throw new RegraNegocioException("A senha deve ter entre 8 e 128 caracteres e não pode conter só espaços.");
            }
            if (!Arrays.equals(senha, confirmacao)) {
                throw new RegraNegocioException("A confirmação da senha não confere.");
            }
            try { return dao.inserir(normalized, SenhaHash.gerar(senha)); }
            catch (PersistenciaException e) {
                if (e.loginDuplicado()) throw new RegraNegocioException("Este usuário já está cadastrado.");
                throw e;
            }
        } finally {
            if (senha != null) Arrays.fill(senha, '\0');
            if (confirmacao != null) Arrays.fill(confirmacao, '\0');
        }
    }

    private static String validarLogin(String login) {
        String normalized = login == null ? "" : login.strip().toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9._-]{3,50}")) {
            throw new RegraNegocioException("Usuário: use de 3 a 50 letras sem acento, números, ponto, hífen ou sublinhado.");
        }
        return normalized;
    }

    private static boolean somenteEspacos(char[] senha) {
        for (char c : senha) if (!Character.isWhitespace(c)) return false;
        return true;
    }
}
