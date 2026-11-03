package br.com.farmacia.ui;

import br.com.farmacia.model.Usuario;
import br.com.farmacia.service.RemedioService;
import br.com.farmacia.service.UsuarioService;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

public final class TelaFarmacia extends JFrame {
    public TelaFarmacia(UsuarioService usuarios, RemedioService remedios, Usuario usuario) {
        super("Sistema Farmácia — Gestão");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1000, 700); setLocationRelativeTo(null);
        setExtendedState(MAXIMIZED_BOTH);
        JMenuBar bar = new JMenuBar();
        JMenu medicamentos = new JMenu("Medicamentos"); medicamentos.setMnemonic('M');
        JMenuItem gerenciar = new JMenuItem("Gerenciar (Cadastrar / Consultar / Editar / Excluir)");
        medicamentos.add(gerenciar);
        JMenu sistema = new JMenu("Sistema"); sistema.setMnemonic('S');
        JMenuItem sair = new JMenuItem("Sair (Logout)"); sistema.add(sair);
        bar.add(medicamentos); bar.add(sistema); setJMenuBar(bar);
        JPanel center = new JPanel(new GridBagLayout());
        JPanel welcome = new JPanel(); welcome.setLayout(new BoxLayout(welcome, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Gestão de medicamentos");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel subtitle = new JLabel("Acesse Medicamentos para cadastrar, consultar, editar e excluir.");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton open = new JButton("Gerenciar medicamentos"); open.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcome.add(title); welcome.add(Box.createVerticalStrut(16)); welcome.add(subtitle);
        welcome.add(Box.createVerticalStrut(24)); welcome.add(open); center.add(welcome);
        add(center, BorderLayout.CENTER);
        JLabel session = new JLabel("Usuário conectado: " + usuario.login());
        session.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16)); add(session, BorderLayout.SOUTH);
        gerenciar.addActionListener(e -> new TelaCrudRemedios(this, remedios).setVisible(true));
        open.addActionListener(e -> gerenciar.doClick());
        sair.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Deseja realmente sair da sua conta?", "Logout",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                new TelaLogin(usuarios, remedios).setVisible(true);
                dispose();
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (JOptionPane.showConfirmDialog(TelaFarmacia.this, "Deseja encerrar o sistema?", "Encerrar",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) System.exit(0);
            }
        });
    }
}
