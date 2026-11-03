package br.com.farmacia;

import br.com.farmacia.config.ConnectionFactory;
import br.com.farmacia.dao.*;
import br.com.farmacia.model.Remedio;
import br.com.farmacia.service.*;
import br.com.farmacia.ui.RemedioTableModel;
import java.math.BigDecimal;
import java.sql.*;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class FluxoTest {
    private RemedioService remedios;
    private UsuarioService usuarios;
    private ConnectionFactory conexoes;

    @BeforeEach void preparar() throws Exception {
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        conexoes = () -> DriverManager.getConnection(url);
        try (Connection c = conexoes.open(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE usuarios(id INT AUTO_INCREMENT PRIMARY KEY, login VARCHAR(50) UNIQUE NOT NULL, senha VARCHAR(255) NOT NULL)");
            s.execute("CREATE TABLE remedios(id INT AUTO_INCREMENT PRIMARY KEY, nome VARCHAR(100) NOT NULL, fabricante VARCHAR(100) NOT NULL, preco DECIMAL(10,2) CHECK(preco>=0), quantidade INT CHECK(quantidade>=0))");
        }
        remedios = new RemedioService(new RemedioDAO(conexoes));
        usuarios = new UsuarioService(new UsuarioDAO(conexoes));
    }

    @Test void cadastroLoginDuplicidadeEEntradaMaliciosa() {
        char[] senha = "Segredo123".toCharArray();
        char[] confirmacao = "Segredo123".toCharArray();
        var usuario = usuarios.cadastrar("  Aluno  ", senha, confirmacao);
        assertEquals("aluno", usuario.login());
        assertArrayEquals(new char[senha.length], senha);
        assertArrayEquals(new char[confirmacao.length], confirmacao);
        assertEquals(usuario, usuarios.autenticar("ALUNO", "Segredo123".toCharArray()).orElseThrow());
        assertTrue(usuarios.autenticar("aluno", "incorreta".toCharArray()).isEmpty());
        assertTrue(usuarios.autenticar("ausente", "Segredo123".toCharArray()).isEmpty());
        assertThrows(RegraNegocioException.class, () -> usuarios.autenticar("' OR 1=1 --", "123".toCharArray()));
        assertThrows(RegraNegocioException.class, () -> usuarios.cadastrar("aluno", "Segredo123".toCharArray(), "Segredo123".toCharArray()));
        String hash = new UsuarioDAO(conexoes).buscarPorLogin("aluno").orElseThrow().senhaHash();
        assertTrue(hash.startsWith("pbkdf2_sha256$"));
    }

    @Test void senhasInvalidasNaoCriamUsuario() {
        assertThrows(RegraNegocioException.class, () -> usuarios.cadastrar("teste", "123".toCharArray(), "123".toCharArray()));
        assertThrows(RegraNegocioException.class, () -> usuarios.cadastrar("teste", "        ".toCharArray(), "        ".toCharArray()));
        assertThrows(RegraNegocioException.class, () -> usuarios.cadastrar("teste", "Segredo123".toCharArray(), "Diferente123".toCharArray()));
        assertTrue(new UsuarioDAO(conexoes).buscarPorLogin("teste").isEmpty());
    }

    @Test void crudPersistePrecisaoAtualizaEExclui() {
        Remedio novo = remedios.validar(0, "  Ácido fólico  ", " Laboratório A ", "12,50", "10");
        int id = remedios.cadastrar(novo);
        assertTrue(id > 0);
        var salvo = remedios.listarTodos().get(0);
        assertEquals("Ácido fólico", salvo.nome());
        assertEquals(new BigDecimal("12.50"), salvo.preco());
        remedios.atualizar(remedios.validar(id, "Ácido fólico", "Laboratório B", "0.10", "0"));
        salvo = remedios.listarTodos().get(0);
        assertEquals("Laboratório B", salvo.fabricante());
        assertEquals(new BigDecimal("0.10"), salvo.preco());
        assertEquals(0, salvo.quantidade());
        remedios.excluir(id);
        assertTrue(remedios.listarTodos().isEmpty());
        assertThrows(RegraNegocioException.class, () -> remedios.excluir(id));
        assertThrows(RegraNegocioException.class, () -> remedios.atualizar(
                remedios.validar(id, "Ausente", "Fabricante", "10", "1")));
    }

    @ParameterizedTest @ValueSource(strings={"-1", "NaN", "Infinity", "1.234,56", "12,345", "100000000", "", "abc"})
    void rejeitaPrecosInvalidos(String preco) {
        assertThrows(RegraNegocioException.class, () -> remedios.validar(0, "Nome", "Fabricante", preco, "1"));
    }

    @ParameterizedTest @ValueSource(strings={"-1", "1.5", "2147483648", "", "abc"})
    void rejeitaQuantidadesInvalidas(String quantidade) {
        assertThrows(RegraNegocioException.class, () -> remedios.validar(0, "Nome", "Fabricante", "1", quantidade));
    }

    @ParameterizedTest
    @CsvSource(delimiter='|', value={
            "d | Letras não entram na quantidade. Informe um número inteiro.",
            "12a | Letras não entram na quantidade. Informe um número inteiro.",
            "-5 | A quantidade não pode ser negativa.",
            "1.5 | Quantidade: informe um número inteiro, sem casas decimais ou símbolos.",
            "999999999999 | Quantidade máxima: 2147483647."
    })
    void quantidadeExplicaOCasoEspecifico(String entrada, String mensagem) {
        var erro = assertThrows(RegraNegocioException.class,
                () -> remedios.validar(0, "Nome", "Fabricante", "1", entrada));
        assertEquals(mensagem, erro.getMessage());
    }

    @Test void validaLimitesTextosEValoresForaDaTela() {
        assertThrows(RegraNegocioException.class, () -> remedios.validar(0, "  ", "Fabricante", "1", "1"));
        assertThrows(RegraNegocioException.class, () -> remedios.validar(0, "Nome", "x".repeat(101), "1", "1"));
        assertThrows(RegraNegocioException.class, () -> remedios.cadastrar(new Remedio(0, "Nome", "Fabricante", new BigDecimal("-1"), 1)));
        assertEquals(new BigDecimal("99999999.99"), remedios.validar(0, "Nome", "Fabricante", "99999999,99", "2147483647").preco());
    }

    @Test void tabelaNaoEditaCelulasEOrdenaNumerosComoNumeros() {
        RemedioTableModel model = new RemedioTableModel();
        model.substituir(java.util.List.of(new Remedio(1, "Nome", "Fabricante", new BigDecimal("10.20"), 5)));
        assertFalse(model.isCellEditable(0, 0));
        assertFalse(model.isCellEditable(0, 3));
        assertEquals(BigDecimal.class, model.getColumnClass(3));
        assertEquals(Integer.class, model.getColumnClass(4));
        assertEquals(new BigDecimal("10.20"), model.getValueAt(0, 3));
    }

    @Test void indisponibilidadeDoBancoNaoViraSenhaIncorreta() {
        UsuarioService service = new UsuarioService(new UsuarioDAO(() -> {
            throw new SQLException("Falha simulada", "08001");
        }));
        assertThrows(PersistenciaException.class, () -> service.autenticar("teste", "Segredo123".toCharArray()));
    }
}

