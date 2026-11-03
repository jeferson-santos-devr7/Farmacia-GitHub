package br.com.farmacia.ui;

import br.com.farmacia.dao.PersistenciaException;
import br.com.farmacia.service.RegraNegocioException;
import java.awt.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

final class Ui {
    private static final Logger LOG = Logger.getLogger(Ui.class.getName());
    private Ui() { }

    static JPanel formulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        return panel;
    }

    static void campo(JPanel panel, int row, String label, JComponent field) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = row; g.anchor = GridBagConstraints.LINE_START;
        g.insets = new Insets(6, 4, 6, 12);
        JLabel l = new JLabel(label);
        l.setLabelFor(field);
        panel.add(l, g);
        g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, g);
    }

    static void erro(Component parent, Throwable error) {
        String message;
        if (error instanceof RegraNegocioException) message = error.getMessage();
        else {
            LOG.log(Level.SEVERE, "Falha na operação", error);
            message = error instanceof PersistenciaException
                    ? "Não foi possível " + error.getMessage() + ".\nVerifique se o MySQL está ligado, se o script SQL foi executado e se a configuração está correta."
                    : "Não foi possível concluir a operação. Consulte os detalhes no console.";
        }
        JOptionPane.showMessageDialog(parent, message, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    /** Executa trabalho fora da EDT e retorna à EDT para atualizar os componentes. */
    static <T> void executar(Component parent, Consumer<Boolean> busy,
                            Callable<T> task, Consumer<T> success) {
        busy.accept(true);
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return task.call(); }
            @Override protected void done() {
                busy.accept(false);
                try { success.accept(get()); }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    erro(parent, e);
                } catch (ExecutionException e) { erro(parent, e.getCause()); }
                catch (RuntimeException e) { erro(parent, e); }
            }
        }.execute();
    }
}
