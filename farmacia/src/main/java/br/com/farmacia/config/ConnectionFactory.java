package br.com.farmacia.config;

import java.sql.Connection;
import java.sql.SQLException;

/** Permite usar JDBC real e bancos isolados nos testes. */
@FunctionalInterface
public interface ConnectionFactory {
    Connection open() throws SQLException;
}
