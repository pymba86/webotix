package ru.webotix.support;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;

public class ReadVersion {

    private ReadVersion() {}

    public static String readVersionInfoInManifest() {
        try (InputStream stream =
                     new BufferedInputStream(ReadVersion.class.getResourceAsStream("/VERSION"))) {
            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
