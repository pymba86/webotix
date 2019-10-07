package ru.webotix.utils;

import io.reactivex.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public final class SafelyDispose {

    private static final Logger log = LoggerFactory.getLogger(SafelyDispose.class);

    private SafelyDispose() {}

    public static void of (Disposable... disposables) {
        of(Arrays.asList(disposables));
    }

    public static void of(Iterable<Disposable> disposables) {
        disposables.forEach(disposable -> {
            if (disposable == null)
                return;
            try {
                disposable.dispose();
            } catch (Exception e) {
                log.error("Error disposing of subscription", e);
            }
        });
    }
}
