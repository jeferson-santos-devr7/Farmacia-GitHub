package br.com.farmacia.dao;

import java.sql.SQLException;

public final class PersistenciaException extends RuntimeException {
    public PersistenciaException(String operation, SQLException cause) {
        super(operation, cause);
    }

    public boolean loginDuplicado() {
        SQLException e = (SQLException) getCause();
        return e.getErrorCode() == 1062 || "23505".equals(e.getSQLState());
    }
}
