package it.podlodowski.javatry.util.retry;

import it.podlodowski.javatry.util.precondition.Preconditions;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class Retry {

    public static final int INFINITE = Integer.MIN_VALUE;

    Retry() throws InstantiationException {
        throw new InstantiationException("Instances of this class are forbidden.");
    }

    public static <T> T runWithRetry(Callable<T> callable, int maxTries, Runnable onRetry, Duration delay) throws Exception {
        int count = 0;
        if (maxTries == 0) return null;
        while (true) {
            try {
                return callable.call();
            } catch (Exception e) {
                if (INFINITE != maxTries && ++count == maxTries) {
                    throw e;
                } else {

                    if (delay != null) {
                        Thread.sleep(delay.toMillis());
                    }

                    if (onRetry != null) {
                        onRetry.run();
                    }
                }
            }
        }
    }

    public static <T> T runWithRetry(Callable<T> callable, int maxTries) throws Exception {
        return runWithRetry(callable, maxTries, null, (Duration) null);
    }

    public static <T> T runWithRetry(Callable<T> callable, int maxTries, Runnable onRetry) throws Exception {
        return runWithRetry(callable, maxTries, onRetry, (Duration) null);
    }

    public static <T> T runWithRetry(Callable<T> callable, int maxTries, Duration delay, Runnable onRetry,
                                     Consumer<Exception> exceptionConsumer) {
        return withExceptionConsumed(() -> runWithRetry(callable, maxTries, onRetry, delay), exceptionConsumer);
    }

    private static <T> T withExceptionConsumed(Callable<T> callable, Consumer<Exception> exceptionConsumer) {
        try {
            return callable.call();
        } catch (Exception e) {
            exceptionConsumer.accept(e);
            return null;
        }
    }
}
