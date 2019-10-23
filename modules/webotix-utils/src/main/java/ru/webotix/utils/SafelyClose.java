package ru.webotix.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class SafelyClose {

    private static final Logger log = LoggerFactory.getLogger(SafelyClose.class);

    private SafelyClose() {
        // Не инстанцируемый
    }

    public static void the(AutoCloseable... closeables) {
        the(Arrays.asList(closeables));
    }

    public static void the(Iterable<AutoCloseable> closeables) {
        closeables.forEach(d -> {
            if (d == null)
                return;
            try {
                d.close();
            } catch (Exception e) {
                log.error("Error when closing resource", e);
            }
        });
    }

}
