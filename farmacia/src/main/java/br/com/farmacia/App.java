package br.com.farmacia;

import br.com.farmacia.config.ConexaoDB;
import br.com.farmacia.dao.RemedioDAO;
import br.com.farmacia.dao.UsuarioDAO;
import br.com.farmacia.service.RemedioService;
import br.com.farmacia.service.UsuarioService;
import br.com.farmacia.ui.TelaLogin;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

public final class App {
    private App() { }
    public static void main(String[] args) {
        try {
            // Configuração e inicialização criptográfica fora da thread da interface.
            ConexaoDB db = ConexaoDB.carregar();
            UsuarioService usuarios = new UsuarioService(new UsuarioDAO(db));
            RemedioService remedios = new RemedioService(new RemedioDAO(db));
            SwingUtilities.invokeLater(() -> {
                try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
                catch (Exception ignored) { /* O tema padrão do Swing continua disponível. */ }
                new TelaLogin(usuarios, remedios).setVisible(true);
            });
        } catch (Exception e) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, "Falha ao iniciar", e);
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, e.getMessage(),
                    "Configuração do Sistema Farmácia", JOptionPane.ERROR_MESSAGE));
        }
    }
}
