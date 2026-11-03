package br.com.farmacia.dao;

import br.com.farmacia.config.ConnectionFactory;
import br.com.farmacia.model.Remedio;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class RemedioDAO {
    private final ConnectionFactory conexoes;
    public RemedioDAO(ConnectionFactory conexoes) { this.conexoes = conexoes; }

    public int inserir(Remedio r) {
        try (Connection c = conexoes.open(); PreparedStatement s = c.prepareStatement(
                "INSERT INTO remedios (nome, fabricante, preco, quantidade) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            preencher(s, r);
            s.executeUpdate();
            try (ResultSet rs = s.getGeneratedKeys()) {
                if (!rs.next()) throw new SQLException("ID não retornado pelo banco.");
                return rs.getInt(1);
            }
        } catch (SQLException e) { throw new PersistenciaException("cadastrar medicamento", e); }
    }

    public List<Remedio> listarTodos() {
        List<Remedio> list = new ArrayList<>();
        try (Connection c = conexoes.open(); PreparedStatement s = c.prepareStatement(
                "SELECT id, nome, fabricante, preco, quantidade FROM remedios ORDER BY nome, id");
                ResultSet rs = s.executeQuery()) {
            while (rs.next()) list.add(new Remedio(rs.getInt("id"), rs.getString("nome"),
                    rs.getString("fabricante"), rs.getBigDecimal("preco"), rs.getInt("quantidade")));
            return list;
        } catch (SQLException e) { throw new PersistenciaException("listar medicamentos", e); }
    }

    public boolean atualizar(Remedio r) {
        try (Connection c = conexoes.open(); PreparedStatement s = c.prepareStatement(
                "UPDATE remedios SET nome=?, fabricante=?, preco=?, quantidade=? WHERE id=?")) {
            preencher(s, r);
            s.setInt(5, r.id());
            return s.executeUpdate() == 1;
        } catch (SQLException e) { throw new PersistenciaException("atualizar medicamento", e); }
    }

    public boolean excluir(int id) {
        try (Connection c = conexoes.open(); PreparedStatement s = c.prepareStatement(
                "DELETE FROM remedios WHERE id=?")) {
            s.setInt(1, id);
            return s.executeUpdate() == 1;
        } catch (SQLException e) { throw new PersistenciaException("excluir medicamento", e); }
    }

    private static void preencher(PreparedStatement s, Remedio r) throws SQLException {
        s.setString(1, r.nome());
        s.setString(2, r.fabricante());
        s.setBigDecimal(3, r.preco());
        s.setInt(4, r.quantidade());
    }
}
