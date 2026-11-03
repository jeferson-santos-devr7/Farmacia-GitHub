package br.com.farmacia.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class ConexaoDB implements ConnectionFactory {
    private final String url;
    private final Properties credentials = new Properties();

    public ConexaoDB(String url, String user, String password) {
        if (url == null || !url.startsWith("jdbc:mysql:")) {
            throw new IllegalArgumentException("Informe uma URL JDBC do MySQL válida.");
        }
        this.url = url;
        credentials.setProperty("user", user);
        credentials.setProperty("password", password);
    }

    public static ConexaoDB carregar() throws IOException {
        Properties p = new Properties();
        Path file = Path.of(System.getProperty("farmacia.config", "config/db.properties"));
        if (Files.isRegularFile(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                p.load(reader);
            }
        }
        String url = valor("FARMACIA_DB_URL", p, "db.url");
        String user = valor("FARMACIA_DB_USER", p, "db.user");
        String password = valor("FARMACIA_DB_PASSWORD", p, "db.password");
        if (url == null || url.isBlank() || user == null || user.isBlank() || password == null) {
            throw new IOException("Configure config/db.properties usando o arquivo .example, "
                    + "ou defina FARMACIA_DB_URL, FARMACIA_DB_USER e FARMACIA_DB_PASSWORD.");
        }
        return new ConexaoDB(url, user, password);
    }

    private static String valor(String env, Properties p, String key) {
        String value = System.getenv(env);
        return value != null ? value : p.getProperty(key);
    }

    @Override
    public Connection open() throws SQLException {
        return DriverManager.getConnection(url, credentials);
    }

    public Connection getConexao() throws SQLException {
        return open();
    }
}
