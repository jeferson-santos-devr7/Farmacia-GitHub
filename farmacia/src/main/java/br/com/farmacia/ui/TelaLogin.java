package br.com.farmacia.ui;

import br.com.farmacia.service.RemedioService;
import br.com.farmacia.service.UsuarioService;
import java.awt.*;
import javax.swing.*;

public final class TelaLogin extends JFrame {
    private final UsuarioService usuarios;
    private final RemedioService remedios;
    private final JTextField usuario = new JTextField(22);
    private final JPasswordField senha = new JPasswordField(22);
    private final JButton entrar = new JButton("Entrar");
    private final JButton cadastrar = new JButton("Cadastre-se");
    private final JButton esqueci = new JButton("Esqueci minha senha");
    private final JLabel status = new JLabel("Informe suas credenciais.");

    public TelaLogin(UsuarioService usuarios, RemedioService remedios) {
        super("Sistema Farmácia — Login");
        this.usuarios = usuarios;
        this.remedios = remedios;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel form = Ui.formulario();
        Ui.campo(form, 0, "Usuário:", usuario);
        Ui.campo(form, 1, "Senha:", senha);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        buttons.add(entrar); buttons.add(cadastrar); buttons.add(esqueci);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(buttons, BorderLayout.CENTER);
        status.setBorder(BorderFactory.createEmptyBorder(4, 16, 12, 16));
        bottom.add(status, BorderLayout.SOUTH);
        add(form, BorderLayout.CENTER); add(bottom, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(entrar);
        entrar.addActionListener(e -> autenticar());
        cadastrar.addActionListener(e -> {
            senha.setText("");
            new TelaCadastro(this, usuarios).setVisible(true);
        });
        esqueci.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Contate o administrador para redefinir a senha.", "Recuperação de acesso", JOptionPane.INFORMATION_MESSAGE));
        pack(); setMinimumSize(getSize()); setLocationRelativeTo(null);
    }

    private void autenticar() {
        String login = usuario.getText();
        char[] password = senha.getPassword();
        senha.setText("");
        Ui.executar(this, this::ocupado, () -> usuarios.autenticar(login, password), result -> {
            if (result.isPresent()) {
                new TelaFarmacia(usuarios, remedios, result.get()).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Usuário ou senha incorretos!", "Login", JOptionPane.WARNING_MESSAGE);
                senha.requestFocusInWindow();
            }
        });
    }

    private void ocupado(boolean busy) {
        usuario.setEnabled(!busy); senha.setEnabled(!busy);
        entrar.setEnabled(!busy); cadastrar.setEnabled(!busy); esqueci.setEnabled(!busy);
        status.setText(busy ? "Autenticando..." : "Informe suas credenciais.");
        setDefaultCloseOperation(busy ? DO_NOTHING_ON_CLOSE : EXIT_ON_CLOSE);
        setCursor(Cursor.getPredefinedCursor(busy ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    void usuarioCadastrado(String login) {
        usuario.setText(login); senha.setText(""); senha.requestFocusInWindow();
    }
}
