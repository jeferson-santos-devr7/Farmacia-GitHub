package br.com.farmacia.ui;

import br.com.farmacia.model.Remedio;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public final class RemedioTableModel extends AbstractTableModel {
    private static final String[] COLUNAS = {"ID", "Nome", "Fabricante", "Preço (R$)", "Quantidade"};
    private List<Remedio> linhas = List.of();
    public void substituir(List<Remedio> dados) { linhas = List.copyOf(dados); fireTableDataChanged(); }
    public Remedio remedio(int linha) { return linhas.get(linha); }
    @Override public int getRowCount() { return linhas.size(); }
    @Override public int getColumnCount() { return COLUNAS.length; }
    @Override public String getColumnName(int coluna) { return COLUNAS[coluna]; }
    @Override public boolean isCellEditable(int linha, int coluna) { return false; }
    @Override public Class<?> getColumnClass(int coluna) {
        return switch (coluna) { case 0, 4 -> Integer.class; case 3 -> BigDecimal.class; default -> String.class; };
    }
    @Override public Object getValueAt(int linha, int coluna) {
        Remedio r = linhas.get(linha);
        return switch (coluna) {
            case 0 -> r.id(); case 1 -> r.nome(); case 2 -> r.fabricante();
            case 3 -> r.preco(); case 4 -> r.quantidade();
            default -> throw new IndexOutOfBoundsException("Coluna inválida");
        };
    }
}
