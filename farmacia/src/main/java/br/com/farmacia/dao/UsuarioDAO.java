package br.com.farmacia.dao;

import br.com.farmacia.config.ConnectionFactory;
import br.com.farmacia.model.Usuario;
import java.sql.*;
import java.util.Optional;

public final class UsuarioDAO {
    private final ConnectionFactory conexoes;
    public UsuarioDAO(ConnectionFactory conexoes) { this.conexoes = conexoes; }

    public record Credencial(Usuario usuario, String senhaHash) { }

    public Optional<Credencial> buscarPorLogin(String login) {
        String sql = "SELECT id, login, senha FROM usuarios WHERE login = ?";
        try (Connection c = conexoes.open(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, login);
            try (ResultSet rs = s.executeQuery()) {
                return rs.next() ? Optional.of(new Credencial(
                        new Usuario(rs.getInt("id"), rs.getString("login")), rs.getString("senha")))
                        : Optional.empty();
            }
        } catch (SQLException e) { throw new PersistenciaException("consultar usuário", e); }
    }

    public Usuario inserir(String login, String hash) {
        try (Connection c = conexoes.open(); PreparedStatement s = c.prepareStatement(
                "INSERT INTO usuarios (login, senha) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1, login);
            s.setString(2, hash);
            s.executeUpdate();
            try (ResultSet rs = s.getGeneratedKeys()) {
                if (!rs.next()) throw new SQLException("ID não retornado pelo banco.");
                return new Usuario(rs.getInt(1), login);
            }
        } catch (SQLException e) { throw new PersistenciaException("cadastrar usuário", e); }
    }
}
