package br.com.farmacia;

import br.com.farmacia.config.ConexaoDB;

public final class TesteConexao {
    private TesteConexao() { }
    public static void main(String[] args) {
        try (var conn = ConexaoDB.carregar().open();
             var stmt = conn.prepareStatement("SELECT COUNT(*) FROM usuarios");
             var rs = stmt.executeQuery()) {
            rs.next();
            System.out.println("SUCESSO: conexão com MySQL e tabela usuarios disponíveis.");
            System.out.println("Usuários cadastrados: " + rs.getInt(1));
        } catch (Exception e) {
            System.err.println("FALHA: " + e.getMessage());
            System.exit(1);
        }
    }
}
