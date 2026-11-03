package br.com.farmacia;

import br.com.farmacia.security.SenhaHash;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SenhaHashTest {
    @Test void hashTemSaltEValidaSomenteASenhaCorreta() {
        String a = SenhaHash.gerar("SenhaValida123".toCharArray());
        String b = SenhaHash.gerar("SenhaValida123".toCharArray());
        assertNotEquals(a, b);
        assertFalse(a.contains("SenhaValida123"));
        assertTrue(SenhaHash.verificar("SenhaValida123".toCharArray(), a));
        assertFalse(SenhaHash.verificar("errada".toCharArray(), a));
    }
    @Test void hashDoScriptCorrespondeAoAdminDoExercicio() {
        assertTrue(SenhaHash.verificar("123".toCharArray(),
                "pbkdf2_sha256$600000$RmFybWFjaWFBdWxhMjAyNg==$BEWMHUeXHMskqVPK2+YAahKz9H0LBnftTQFqBmzOJvw="));
    }
    @Test void formatoInvalidoNaoAutentica() {
        for (String hash : new String[]{"123", "", "pbkdf2_sha256$1$x$y", "pbkdf2_sha256$600000$!!!$!!!"}) {
            assertFalse(SenhaHash.verificar("123".toCharArray(), hash));
        }
        assertFalse(SenhaHash.verificar("123".toCharArray(), null));
    }
}
