package ru.webotix.exchange;

import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

public class RateController {

    private static final Logger log = LoggerFactory.getLogger(RateController.class);
    private static final int DEGREES_PERMITTED = 4;
    private static final int DIVISOR = 3;
    private static final double BACKOFF_RATIO = 0.8;

    private final String exchangeName;
    private final RateLimiter rateLimiter;
    private final long throttleBy;
    private final double defaultRate;

    private volatile long throttleExpiryTime;
    private volatile int throttleLevel;
    private final AtomicBoolean reducedRate = new AtomicBoolean();

    /**
     * Контроль ограничения скорости операций по бирже
     *
     * @param exchangeName название биржы
     * @param rateLimiter  Основной ограничитель скорости.
     * @param throttleBy   Продолжительность регулирования должна продолжаться.
     */
    RateController(String exchangeName, RateLimiter rateLimiter, Duration throttleBy) {
        this.exchangeName = exchangeName;
        this.rateLimiter = rateLimiter;
        this.throttleBy = throttleBy.toMillis();
        this.defaultRate = rateLimiter.getRate();
    }

    public void acquire() {
        rateLimiter.acquire();
        log.debug("Acquired API ticket for {}", exchangeName);
        if (throttleExpired()) {
            synchronized (this) {
                if (throttleExpired()) {
                    throttleExpiryTime = 0L;
                    throttleLevel = 0;
                    rateLimiter.setRate(defaultRate * (reducedRate.get() ? BACKOFF_RATIO : 1));
                    log.info("Throttle on {} expired. Restored rate to {} calls/sec",
                            exchangeName, rateLimiter.getRate());
                }
            }
        }
    }

    /**
     * Значительно снижает пропускную способность на временной основе.
     * Этот газ истечет через некоторое время.
     */
    public void throttle() {
        if (canThrottleFurther()) {
            synchronized (this) {
                if (canThrottleFurther()) {
                    throttleExpiryTime = System.currentTimeMillis() + throttleBy;
                    throttleLevel++;
                    rateLimiter.setRate((rateLimiter.getRate()) / DIVISOR);
                    log.info("Throttled {} rate to {} calls/sec", exchangeName, rateLimiter.getRate());
                }
            }
        }
    }

    public void backoff() {
        if (reducedRate.compareAndSet(false, true)) {
            synchronized (this) {
                rateLimiter.setRate(rateLimiter.getRate() * BACKOFF_RATIO);
                log.info("Permanently reduced {} rate by {}", exchangeName, BACKOFF_RATIO);
            }
        }
    }

    public void pause() {
        for (int i = 0; i < DEGREES_PERMITTED; i++)
            throttle();
    }

    private boolean canThrottleFurther() {
        return throttleLevel < DEGREES_PERMITTED;
    }

    private boolean throttleExpired() {
        return throttleLevel != 0 && throttleExpiryTime < System.currentTimeMillis();
    }

}
