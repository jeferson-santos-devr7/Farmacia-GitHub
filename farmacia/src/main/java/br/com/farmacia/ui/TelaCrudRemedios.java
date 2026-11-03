package br.com.farmacia.ui;

import br.com.farmacia.model.Remedio;
import br.com.farmacia.service.RemedioService;
import br.com.farmacia.service.RegraNegocioException;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;

public final class TelaCrudRemedios extends JDialog {
    private final RemedioService service;
    private final JTextField id = new JTextField(22);
    private final JTextField nome = new JTextField(22);
    private final JTextField fabricante = new JTextField(22);
    private final JTextField preco = new JTextField(22);
    private final JTextField quantidade = new JTextField(22);
    private final JTextField busca = new JTextField(24);
    private final JButton cadastrar = new JButton("Cadastrar");
    private final JButton atualizar = new JButton("Atualizar");
    private final JButton excluir = new JButton("Excluir");
    private final JButton limpar = new JButton("Limpar");
    private final JButton recarregar = new JButton("Atualizar lista");
    private final JButton fechar = new JButton("Fechar");
    private final RemedioTableModel model = new RemedioTableModel();
    private final JTable tabela = new JTable(model);
    private final TableRowSorter<RemedioTableModel> sorter = new TableRowSorter<>(model);
    private final JLabel status = new JLabel("Carregando medicamentos...");
    private boolean busy;

    public TelaCrudRemedios(JFrame parent, RemedioService service) {
        super(parent, "Gerenciar medicamentos", true);
        this.service = service;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel form = Ui.formulario();
        id.setEditable(false); id.setFocusable(false);
        Ui.campo(form, 0, "ID (automático):", id);
        Ui.campo(form, 1, "Nome:", nome);
        Ui.campo(form, 2, "Fabricante:", fabricante);
        Ui.campo(form, 3, "Preço (R$):", preco);
        Ui.campo(form, 4, "Quantidade:", quantidade);
        preco.setToolTipText("Ex.: 12,50. Não use separador de milhar.");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        for (JButton b : new JButton[]{cadastrar, atualizar, excluir, limpar}) buttons.add(b);
        JPanel north = new JPanel(new BorderLayout());
        north.add(form, BorderLayout.CENTER); north.add(buttons, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);
        JPanel consultation = new JPanel(new BorderLayout(8, 8));
        consultation.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        JPanel search = new JPanel(new BorderLayout(8, 0));
        JLabel label = new JLabel("Buscar nome ou fabricante:"); label.setLabelFor(busca);
        search.add(label, BorderLayout.WEST); search.add(busca, BorderLayout.CENTER);
        search.add(recarregar, BorderLayout.EAST);
        consultation.add(search, BorderLayout.NORTH);
        tabela.setRowSorter(sorter); tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setFillsViewportHeight(true); tabela.setRowHeight(26);
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(45);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(230);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(200);
        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
        tabela.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override protected void setValue(Object value) {
                setHorizontalAlignment(SwingConstants.RIGHT);
                setText(value == null ? "" : currency.format(value));
            }
        });
        consultation.add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(consultation, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(4, 16, 12, 16));
        bottom.add(status, BorderLayout.CENTER); bottom.add(fechar, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);
        cadastrar.addActionListener(e -> salvar(false));
        atualizar.addActionListener(e -> salvar(true));
        excluir.addActionListener(e -> excluir());
        limpar.addActionListener(e -> limparCampos());
        recarregar.addActionListener(e -> carregarTabela());
        fechar.addActionListener(e -> dispose());
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !busy) preencherForm();
        });
        busca.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrar(); }
        });
        setSize(840, 680); setMinimumSize(new Dimension(720, 580)); setLocationRelativeTo(parent);
        acoes();
        carregarTabela();
    }

    private void salvar(boolean edicao) {
        try {
            Remedio r = service.validar(edicao ? idSelecionado() : 0, nome.getText(),
                    fabricante.getText(), preco.getText(), quantidade.getText());
            Ui.executar(this, this::ocupado, () -> {
                if (edicao) service.atualizar(r); else service.cadastrar(r);
                return true;
            }, ok -> {
                JOptionPane.showMessageDialog(this, edicao ? "Atualizado com sucesso!" : "Cadastrado com sucesso!");
                limparCampos(); carregarTabela();
            });
        } catch (RegraNegocioException e) { Ui.erro(this, e); }
    }

    private void excluir() {
        try {
            int selected = idSelecionado();
            if (JOptionPane.showConfirmDialog(this, "Deseja excluir o medicamento “" + nome.getText() + "”?",
                    "Confirmar exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
            Ui.executar(this, this::ocupado, () -> { service.excluir(selected); return true; }, ok -> {
                JOptionPane.showMessageDialog(this, "Excluído com sucesso!");
                limparCampos(); carregarTabela();
            });
        } catch (RegraNegocioException e) { Ui.erro(this, e); }
    }

    private int idSelecionado() {
        if (id.getText().isEmpty()) throw new RegraNegocioException("Selecione um medicamento na tabela primeiro.");
        return Integer.parseInt(id.getText());
    }

    private void carregarTabela() {
        Ui.executar(this, this::ocupado, service::listarTodos, this::mostrarLista);
    }

    private void mostrarLista(List<Remedio> list) {
        model.substituir(list); limparCampos(); contagem();
    }

    private void preencherForm() {
        int row = tabela.getSelectedRow();
        if (row < 0) { limparCampos(); return; }
        Remedio r = model.remedio(tabela.convertRowIndexToModel(row));
        id.setText(Integer.toString(r.id())); nome.setText(r.nome()); fabricante.setText(r.fabricante());
        preco.setText(r.preco().toPlainString().replace('.', ','));
        quantidade.setText(Integer.toString(r.quantidade())); acoes();
    }

    private void limparCampos() {
        id.setText(""); nome.setText(""); fabricante.setText(""); preco.setText(""); quantidade.setText("");
        if (tabela.getSelectedRow() >= 0) tabela.clearSelection();
        acoes(); nome.requestFocusInWindow();
    }

    private void filtrar() {
        sorter.setRowFilter(busca.getText().isBlank() ? null
                : RowFilter.regexFilter("(?iu)" + Pattern.quote(busca.getText().strip()), 1, 2));
        // Uma seleção escondida pelo filtro não pode continuar como alvo de exclusão.
        if (tabela.getSelectedRow() < 0) limparCampos();
        contagem();
    }

    private void contagem() {
        status.setText(tabela.getRowCount() + " de " + model.getRowCount() + " medicamento(s).");
    }

    private void ocupado(boolean value) {
        busy = value;
        for (JComponent c : new JComponent[]{nome, fabricante, preco, quantidade, busca, tabela,
                limpar, recarregar, fechar}) c.setEnabled(!value);
        acoes();
        setDefaultCloseOperation(value ? DO_NOTHING_ON_CLOSE : DISPOSE_ON_CLOSE);
        setCursor(Cursor.getPredefinedCursor(value ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
        if (value) status.setText("Aguarde..."); else contagem();
    }

    private void acoes() {
        boolean selected = !id.getText().isEmpty();
        cadastrar.setEnabled(!busy && !selected);
        atualizar.setEnabled(!busy && selected); excluir.setEnabled(!busy && selected);
    }
}
