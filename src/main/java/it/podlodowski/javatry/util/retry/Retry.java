package it.podlodowski.javatry.util.retry;

import it.podlodowski.javatry.util.precondition.Preconditions;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class Retry {

    Retry() throws InstantiationException {
        throw new InstantiationException("Instances of this class are forbidden.");
    }

    public static <T> T runWithRetry(Callable<T> callable, int maxTries, Runnable onRetry) throws Exception {
        Preconditions.checkArgument(maxTries > 0, "maxTries must be positive number");
        int count = 0;
        while (true) {
            try {
                return callable.call();
            } catch (Exception e) {
                if (++count == maxTries) {
                    throw e;
                } else {
                    if (onRetry != null) {
                        onRetry.run();
                    }
                }
            }
        }
    }

    public static <T> T runWithRetry(Callable<T> callable, int maxTries) throws Exception {
        return runWithRetry(callable, maxTries, null);
    }

    public static <T> T runWithRetry(Callable<T> callable, int maxTries, Runnable onRetry,
                                     Consumer<Exception> exceptionConsumer) {
        return withExceptionConsumed(() -> runWithRetry(callable, maxTries, onRetry), exceptionConsumer);
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
