package it.podlodowski.javatry;

import it.podlodowski.javatry.util.consumer.Consumers;
import it.podlodowski.javatry.util.retry.Retry;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Try<T> {

    private final Callable<T> callable;

    private int maxTries = 1;
    private Consumer<Exception> exceptionConsumer = Consumers.doNothing();
    private Runnable onRetry;
    private Duration delay = null;

    private Try(Callable<T> callable) {
        this.callable = callable;
    }

    public static <T> Try<T> it(Callable<T> callable) {
        return new Try<>(callable);
    }


    public Try<T> times(int maxTries) {
        if (maxTries >= 0) {
            this.maxTries = maxTries;
        } else {
            throw new IllegalArgumentException("times must not be negative number");
        }
        return this;
    }

    public Try<T> withDelay(Duration delay) {
        this.delay = delay;
        return this;
    }

    public Try<T> onRetry(Runnable onRetry) {
        this.onRetry = onRetry;
        return this;
    }

    public Try<T> onException(Consumer<Exception> exceptionConsumer) {
        this.exceptionConsumer = exceptionConsumer;
        return this;
    }


    public T orThrow() throws Exception {
        return Retry.runWithRetry(callable, maxTries);
    }

    public <E extends Throwable> T orThrow(Supplier<E> exception) throws E {
        try {
            return Retry.runWithRetry(callable, maxTries, onRetry, delay);
        } catch (Exception e) {
            throw exception.get();
        }
    }

    public Optional<T> now() {
        return Optional.ofNullable(Retry.runWithRetry(callable, maxTries, delay, onRetry, exceptionConsumer));
    }
}
