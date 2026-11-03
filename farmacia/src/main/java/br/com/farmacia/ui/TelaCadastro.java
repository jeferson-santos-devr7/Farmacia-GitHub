package br.com.farmacia.ui;

import br.com.farmacia.service.UsuarioService;
import java.awt.*;
import java.util.Arrays;
import javax.swing.*;

public final class TelaCadastro extends JDialog {
    private final JTextField usuario = new JTextField(24);
    private final JPasswordField senha = new JPasswordField(24);
    private final JPasswordField confirmacao = new JPasswordField(24);
    private final JButton cadastrar = new JButton("Cadastrar");
    private final JButton voltar = new JButton("Voltar ao login");
    private final JLabel status = new JLabel("Senha: de 8 a 128 caracteres.");

    public TelaCadastro(TelaLogin parent, UsuarioService service) {
        super(parent, "Novo usuário", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel form = Ui.formulario();
        Ui.campo(form, 0, "Novo usuário:", usuario);
        Ui.campo(form, 1, "Nova senha:", senha);
        Ui.campo(form, 2, "Confirmar senha:", confirmacao);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(voltar); buttons.add(cadastrar);
        JPanel bottom = new JPanel(new BorderLayout());
        status.setBorder(BorderFactory.createEmptyBorder(0, 16, 8, 16));
        bottom.add(status, BorderLayout.NORTH); bottom.add(buttons, BorderLayout.SOUTH);
        add(form, BorderLayout.CENTER); add(bottom, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(cadastrar);
        voltar.addActionListener(e -> dispose());
        cadastrar.addActionListener(e -> {
            String login = usuario.getText();
            char[] pass = senha.getPassword();
            char[] confirmation = confirmacao.getPassword();
            senha.setText(""); confirmacao.setText("");
            Ui.executar(this, this::ocupado, () -> {
                try { return service.cadastrar(login, pass, confirmation); }
                finally { Arrays.fill(pass, '\0'); Arrays.fill(confirmation, '\0'); }
            }, result -> {
                JOptionPane.showMessageDialog(this, "Usuário cadastrado com sucesso!");
                parent.usuarioCadastrado(result.login());
                dispose();
            });
        });
        pack(); setMinimumSize(getSize()); setLocationRelativeTo(parent);
    }

    private void ocupado(boolean busy) {
        usuario.setEnabled(!busy); senha.setEnabled(!busy); confirmacao.setEnabled(!busy);
        cadastrar.setEnabled(!busy); voltar.setEnabled(!busy);
        setDefaultCloseOperation(busy ? DO_NOTHING_ON_CLOSE : DISPOSE_ON_CLOSE);
        status.setText(busy ? "Cadastrando..." : "Senha: de 8 a 128 caracteres.");
        setCursor(Cursor.getPredefinedCursor(busy ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
    }
}
