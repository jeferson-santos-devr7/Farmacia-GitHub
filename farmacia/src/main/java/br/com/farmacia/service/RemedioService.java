package br.com.farmacia.service;

import br.com.farmacia.dao.RemedioDAO;
import br.com.farmacia.model.Remedio;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class RemedioService {
    private final RemedioDAO dao;
    public RemedioService(RemedioDAO dao) { this.dao = dao; }

    public Remedio validar(int id, String nome, String fabricante, String preco, String quantidade) {
        String n = texto(nome, "Nome");
        String f = texto(fabricante, "Fabricante");
        String p = preco == null ? "" : preco.strip();
        if (!p.matches("[0-9]{1,8}([.,][0-9]{1,2})?")) {
            throw new RegraNegocioException("Preço: informe de 0 a 99999999,99, sem separador de milhar e com até 2 casas decimais.");
        }
        BigDecimal valor = new BigDecimal(p.replace(',', '.')).setScale(2, RoundingMode.UNNECESSARY);
        String q = quantidade == null ? "" : quantidade.strip();
        if (q.isEmpty()) throw new RegraNegocioException("Informe a quantidade.");
        if (q.codePoints().anyMatch(Character::isLetter)) {
            throw new RegraNegocioException("Letras não entram na quantidade. Informe um número inteiro.");
        }
        if (q.matches("-[0-9]+")) {
            throw new RegraNegocioException("A quantidade não pode ser negativa.");
        }
        if (!q.matches("[0-9]+")) {
            throw new RegraNegocioException("Quantidade: informe um número inteiro, sem casas decimais ou símbolos.");
        }
        int qtd;
        try { qtd = Integer.parseInt(q); }
        catch (NumberFormatException e) { throw new RegraNegocioException("Quantidade máxima: 2147483647."); }
        if (id < 0) throw new RegraNegocioException("ID inválido.");
        return new Remedio(id, n, f, valor, qtd);
    }

    public int cadastrar(Remedio r) { return dao.inserir(revalidar(r)); }
    public void atualizar(Remedio r) {
        exigirId(r.id());
        if (!dao.atualizar(revalidar(r))) throw new RegraNegocioException("O medicamento não existe mais. Atualize a lista.");
    }
    public void excluir(int id) {
        exigirId(id);
        if (!dao.excluir(id)) throw new RegraNegocioException("O medicamento já foi excluído. Atualize a lista.");
    }
    public List<Remedio> listarTodos() { return dao.listarTodos(); }

    private Remedio revalidar(Remedio r) {
        if (r == null || r.preco() == null) throw new RegraNegocioException("Informe os dados do medicamento.");
        return validar(r.id(), r.nome(), r.fabricante(), r.preco().toPlainString(), Integer.toString(r.quantidade()));
    }
    private static String texto(String valor, String campo) {
        String text = valor == null ? "" : valor.strip();
        if (text.isEmpty() || text.codePointCount(0, text.length()) > 100) {
            throw new RegraNegocioException(campo + ": informe entre 1 e 100 caracteres.");
        }
        return text;
    }
    private static void exigirId(int id) {
        if (id <= 0) throw new RegraNegocioException("Selecione um medicamento na tabela primeiro.");
    }
}
