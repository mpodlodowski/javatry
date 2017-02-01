package it.podlodowski.javatry;

import it.podlodowski.javatry.util.consumer.Consumers;
import it.podlodowski.javatry.util.retry.Retry;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
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


    public EndlessTry<T> endless() {
        return new EndlessTry<>(callable, exceptionConsumer, onRetry, delay);
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

    public CompletableFuture<T> future() {
        return CompletableFuture.supplyAsync(() ->
                Retry.runWithRetry(callable, maxTries, delay, onRetry, exceptionConsumer));
    }

    public Optional<T> now() {
        return Optional.ofNullable(Retry.runWithRetry(callable, maxTries, delay, onRetry, exceptionConsumer));
    }

    public static class EndlessTry<T> {

        private final Callable<T> callable;

        private final int maxTries = Retry.INFINITE;
        private Consumer<Exception> exceptionConsumer;
        private Runnable onRetry;
        private Duration delay;

        public EndlessTry(final Callable<T> callable, final Consumer<Exception> exceptionConsumer,
                          final Runnable onRetry, final Duration delay) {
            this.callable = callable;
            this.exceptionConsumer = exceptionConsumer;
            this.onRetry = onRetry;
            this.delay = delay;
        }

        public EndlessTry<T> withDelay(Duration delay) {
            this.delay = delay;
            return this;
        }

        public EndlessTry<T> onRetry(Runnable onRetry) {
            this.onRetry = onRetry;
            return this;
        }

        public EndlessTry<T> onException(Consumer<Exception> exceptionConsumer) {
            this.exceptionConsumer = exceptionConsumer;
            return this;
        }

        public CompletableFuture<T> future() {
            return CompletableFuture.supplyAsync(() ->
                    Retry.runWithRetry(callable, maxTries, delay, onRetry, exceptionConsumer));
        }
    }
}
