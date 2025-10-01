package pt.ulusofona.tfc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class LeExemplosTest {




    @Test
    void retornaListaVaziaQuandoPastaForNull() {
        ArrayList<String> res = Main.leExemplos(null);
        assertNotNull(res);
        assertTrue(res.isEmpty(), "Para pasta null deve devolver lista vazia.");
    }

    @Test
    void retornaListaVaziaQuandoPastaNaoExiste() {
        File inexistente = new File("___esta_pasta_nao_existe___");
        if (inexistente.exists()) {
            fail("O caminho de teste já existe, escolhe outro nome único.");
        }

        ArrayList<String> res = Main.leExemplos(inexistente);
        assertNotNull(res);
        assertTrue(res.isEmpty(), "Pasta inexistente.");
    }

    @Test
    void retornaListaVaziaQuandoPastaVazia(@TempDir Path tempDir) {
        File pastaVazia = tempDir.toFile();
        ArrayList<String> res = Main.leExemplos(pastaVazia);
        assertNotNull(res);
        assertTrue(res.isEmpty(), "Para pasta vazia deve devolver lista vazia.");
    }


}