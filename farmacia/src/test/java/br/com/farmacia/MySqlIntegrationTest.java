package br.com.farmacia;

import br.com.farmacia.config.ConnectionFactory;
import br.com.farmacia.dao.*;
import br.com.farmacia.security.SenhaHash;
import br.com.farmacia.service.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Executa SOMENTE em servidor MySQL de testes explicitamente informado. */
class MySqlIntegrationTest {
    @Test void scriptReexecutavelAutenticacaoCrudEConstraintsNoMySqlReal() throws Exception {
        String url = System.getenv("FARMACIA_TEST_MYSQL_URL");
        assumeTrue(url != null && !url.isBlank(), "MySQL externo não configurado; execute com FARMACIA_TEST_MYSQL_URL.");
        String user = System.getenv("FARMACIA_TEST_MYSQL_USER");
        String password = System.getenv("FARMACIA_TEST_MYSQL_PASSWORD");
        ConnectionFactory factory = () -> DriverManager.getConnection(url, user, password);
        String script = Files.readString(Path.of("database/script.sql"), StandardCharsets.UTF_8)
                .replaceAll("(?m)^--.*$", "");
        try (Connection c = factory.open(); Statement s = c.createStatement()) {
            for (int round = 0; round < 2; round++) {
                for (String command : script.split(";")) if (!command.isBlank()) s.execute(command);
            }
        }
        // A URL deve selecionar farmacia_db, criado pelo script acima.
        UsuarioService usuarios = new UsuarioService(new UsuarioDAO(factory));
        assertEquals("admin", usuarios.autenticar("admin", "123".toCharArray()).orElseThrow().login());
        String login = "teste_" + Long.toUnsignedString(System.nanoTime());
        usuarios.cadastrar(login, "SenhaTeste123".toCharArray(), "SenhaTeste123".toCharArray());
        assertTrue(usuarios.autenticar(login, "SenhaTeste123".toCharArray()).isPresent());
        assertThrows(RegraNegocioException.class, () -> usuarios.cadastrar(login, "SenhaTeste123".toCharArray(), "SenhaTeste123".toCharArray()));
        RemedioService remedios = new RemedioService(new RemedioDAO(factory));
        int id = remedios.cadastrar(remedios.validar(0, "Teste integração çã", "Fabricante", "19,99", "3"));
        try {
            assertTrue(remedios.listarTodos().stream().anyMatch(r -> r.id() == id && r.preco().toPlainString().equals("19.99")));
            remedios.atualizar(remedios.validar(id, "Atualizado", "Fabricante", "0.10", "0"));
            // Atualização sem alteração também deve ser considerada sucesso.
            remedios.atualizar(remedios.validar(id, "Atualizado", "Fabricante", "0.10", "0"));
            try (Connection c = factory.open(); Statement s = c.createStatement()) {
                assertThrows(SQLException.class, () -> s.executeUpdate("INSERT INTO remedios(nome,fabricante,preco,quantidade) VALUES('Inválido','Teste',-1,0)"));
                assertThrows(SQLException.class, () -> s.executeUpdate("INSERT INTO remedios(nome,fabricante,preco,quantidade) VALUES('Inválido','Teste',1,-1)"));
                assertThrows(SQLException.class, () -> s.executeUpdate("INSERT INTO remedios(nome,fabricante,preco,quantidade) VALUES(' ','Teste',1,0)"));
                assertEquals("MySQL", c.getMetaData().getDatabaseProductName());
                System.out.println("MySQL real validado: " + c.getMetaData().getDatabaseProductVersion());
            }
        } finally {
            remedios.excluir(id);
            try (Connection c = factory.open(); PreparedStatement s = c.prepareStatement("DELETE FROM usuarios WHERE login=?")) {
                s.setString(1, login); s.executeUpdate();
            }
        }
        assertFalse(remedios.listarTodos().stream().anyMatch(r -> r.id() == id));
    }
}
