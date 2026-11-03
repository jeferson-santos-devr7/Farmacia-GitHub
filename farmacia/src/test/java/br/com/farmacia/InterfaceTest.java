package br.com.farmacia;

import br.com.farmacia.config.ConnectionFactory;
import br.com.farmacia.dao.*;
import br.com.farmacia.model.Usuario;
import br.com.farmacia.service.*;
import br.com.farmacia.ui.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class InterfaceTest {
    @Test void tabelaCarregaOrdenaSelecionaFiltraELimpaSemEditarDados() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "Teste Swing precisa de ambiente gráfico.");
        String url = "jdbc:h2:mem:ui_" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        ConnectionFactory factory = () -> DriverManager.getConnection(url);
        try (Connection c = factory.open(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE remedios(id INT AUTO_INCREMENT PRIMARY KEY, nome VARCHAR(100), fabricante VARCHAR(100), preco DECIMAL(10,2), quantidade INT)");
        }
        RemedioService service = new RemedioService(new RemedioDAO(factory));
        service.cadastrar(service.validar(0, "Medicamento A", "Laboratório Alfa", "2,30", "12"));
        service.cadastrar(service.validar(0, "Medicamento B", "Laboratório Beta", "10,20", "5"));
        JFrame parent = edt(JFrame::new);
        TelaCrudRemedios dialog = edt(() -> new TelaCrudRemedios(parent, service));
        try {
            JTable table = edt(() -> encontrar(dialog, JTable.class));
            long limite = System.nanoTime() + 10_000_000_000L;
            while (edt(table::getRowCount) != 2 && System.nanoTime() < limite) Thread.sleep(25);
            edt(() -> {
                assertEquals(2, table.getRowCount());
                assertFalse(table.isCellEditable(0, 0));
                table.getRowSorter().toggleSortOrder(3);
                table.getRowSorter().toggleSortOrder(3);
                table.setRowSelectionInterval(0, 0);
                assertEquals("Medicamento B", campo(dialog, "Nome:").getText());
                assertEquals("10,20", campo(dialog, "Preço (R$):").getText());
                assertTrue(botao(dialog, "Atualizar").isEnabled());
                assertFalse(botao(dialog, "Cadastrar").isEnabled());
                JTextField search = campo(dialog, "Buscar nome ou fabricante:");
                search.setText("Alfa");
                assertEquals(1, table.getRowCount());
                assertEquals("", campo(dialog, "ID (automático):").getText());
                assertFalse(botao(dialog, "Excluir").isEnabled());
                search.setText("["); // Busca literal, não expressão regular.
                assertEquals(0, table.getRowCount());
                search.setText("");
                table.setRowSelectionInterval(0, 0);
                botao(dialog, "Limpar").doClick();
                assertEquals(-1, table.getSelectedRow());
                assertEquals("", campo(dialog, "Nome:").getText());
                assertTrue(botao(dialog, "Cadastrar").isEnabled());
                preview(dialog, "medicamentos.png");
                return null;
            });
            UsuarioService users = new UsuarioService(new UsuarioDAO(factory));
            edt(() -> {
                TelaLogin login = new TelaLogin(users, service);
                TelaCadastro cadastro = new TelaCadastro(login, users);
                TelaFarmacia principal = new TelaFarmacia(users, service, new Usuario(1, "admin"));
                try {
                    preview(login, "login.png"); preview(cadastro, "cadastro.png");
                    principal.setExtendedState(JFrame.NORMAL);
                    preview(principal, "principal.png");
                } finally { cadastro.dispose(); login.dispose(); principal.dispose(); }
                return null;
            });
        } finally { edt(() -> { dialog.dispose(); parent.dispose(); return null; }); }
    }

    private static <T> T edt(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        SwingUtilities.invokeAndWait(task); return task.get();
    }

    private static <T extends Component> T encontrar(Container root, Class<T> type) {
        for (Component c : root.getComponents()) {
            if (type.isInstance(c)) return type.cast(c);
            if (c instanceof Container child) {
                T found = encontrar(child, type); if (found != null) return found;
            }
        }
        return null;
    }

    private static JTextField campo(Container root, String label) {
        for (Component c : root.getComponents()) {
            if (c instanceof JLabel l && label.equals(l.getText()) && l.getLabelFor() instanceof JTextField field) return field;
            if (c instanceof Container child) {
                JTextField found = campo(child, label); if (found != null) return found;
            }
        }
        return null;
    }

    private static JButton botao(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JButton b && text.equals(b.getText())) return b;
            if (c instanceof Container child) {
                JButton found = botao(child, text); if (found != null) return found;
            }
        }
        return null;
    }

    private static void preview(Window window, String name) throws Exception {
        String dir = System.getProperty("farmacia.preview.dir");
        if (dir == null) return;
        window.addNotify(); window.validate();
        Container content = window instanceof JFrame f ? f.getContentPane() : ((JDialog) window).getContentPane();
        BufferedImage image = new BufferedImage(content.getWidth(), content.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try { content.printAll(graphics); } finally { graphics.dispose(); }
        Files.createDirectories(Path.of(dir));
        ImageIO.write(image, "png", Path.of(dir, name).toFile());
    }
}
