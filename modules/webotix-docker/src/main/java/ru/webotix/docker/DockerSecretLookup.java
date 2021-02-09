package ru.webotix.docker;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import io.dropwizard.configuration.UndefinedEnvironmentVariableException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.text.StrLookup;

class DockerSecretLookup extends StrLookup<Object> {

    /* Magic string to allow secrets to be empty. */
    static final String BLANK = ".empty.";

    /* Log which we can spit out later during startup */
    private final List<String> log = new CopyOnWriteArrayList<>();

    private final boolean strict;
    private final boolean enabled;
    private final String path;

    /**
     * Create a new instance.
     *
     * @param strict {@code true} if looking up undefined environment variables should throw a {@link
     *     UndefinedEnvironmentVariableException}, {@code false} otherwise.
     * @throws UndefinedEnvironmentVariableException if the environment variable doesn't exist and
     *     strict behavior is enabled.
     */
    DockerSecretLookup(boolean strict) {
        this("/run/secrets", strict);
    }

    @VisibleForTesting
    DockerSecretLookup(String path, boolean strict) {
        this.path = path;
        this.enabled = new File(path).exists();
        log.add("Docker secrets enabled = " + this.enabled);
        this.strict = strict;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if the Docker secret doesn't exist and strict behavior is
     *     enabled.
     */
    @Override
    public String lookup(String key) {
        if (!enabled && !strict) {
            return key.startsWith("secret-") ? "" : null;
        }
        Preconditions.checkArgument(
                !key.contains(File.separator) && !key.contains("/"), "Path separator in variable name");
        File file = new File(path + File.separator + key);
        String value = null;
        if (file.exists()) {
            try {
                value = Files.asCharSource(file, StandardCharsets.UTF_8).read();
            } catch (IOException e) {
                throw new RuntimeException("IOException when scanning for " + key, e);
            }
            log.add(
                    "Found value for " + key + " (length=" + (value == null ? "-1" : value.length()) + ")");
        }
        if (value == null && strict) {
            throw new IllegalArgumentException(
                    "Docker secret for '"
                            + key
                            + "' is not defined; could not substitute the expression '${"
                            + key
                            + "}'.");
        }
        if (value == null && key.startsWith("secret-")) {
            return "";
        }
        if (BLANK.equals(value)) {
            log.add(" - treating as blank");
            return "";
        }
        return value;
    }

    List<String> getLog() {
        return log;
    }
}
