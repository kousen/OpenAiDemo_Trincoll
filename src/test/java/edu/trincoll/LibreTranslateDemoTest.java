package edu.trincoll;

import org.junit.jupiter.api.Test;

import static edu.trincoll.LibreTranslateDemo.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LibreTranslateDemoTest {
    private final LibreTranslateDemo demo = new LibreTranslateDemo();

    @Test
    void translate() {
        var request = new TranslateRequest("en", "es", "Hello, World!");
        var response = demo.translate(request);
        assertEquals("¡Hola, Mundo!", response);
    }

    @Test
    void translateParagraph() {
        var request = new TranslateRequest("en", "es", """
                Free and Open Source Machine Translation API, entirely self-hosted.
                Unlike other APIs, it doesn't rely on proprietary providers such
                as Google or Azure to perform translations. Instead, its translation
                engine is powered by the open source Argos Translate library.""");
        var response = demo.translate(request);
        assertEquals("""
                Free and Open Source Machine Translation API, completamente auto-hosted.
                A diferencia de otras API, no depende de proveedores propietarios tales
                como Google o Azure para realizar traducciones. En su lugar, su traducción
                motor es alimentado por la biblioteca de código abierto Argos Translate."""
                        .replaceAll("\\n", " ").trim(),
                response.replaceAll("\\n", " ").trim());
    }
}