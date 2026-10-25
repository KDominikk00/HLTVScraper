package com.example.csstats.infrastructure.bo3;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/** Pages saved from bo3.gg, see src/test/resources/fixtures/bo3. */
final class Fixtures {

    private Fixtures() {
    }

    static Document page(String name) {
        try (InputStream stream = Fixtures.class.getResourceAsStream("/fixtures/bo3/" + name + ".html")) {
            return Jsoup.parse(new String(stream.readAllBytes()), "https://bo3.gg");
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
