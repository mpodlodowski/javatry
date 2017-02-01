package it.podlodowski.javatry;

import it.podlodowski.javatry.util.test.MagicException;
import it.podlodowski.javatry.util.test.MagicStatus;
import it.podlodowski.javatry.util.test.MagicWizard;
import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Duration;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static it.podlodowski.javatry.util.test.MagicStatus.DONE;
import static it.podlodowski.javatry.util.test.MagicStatus.FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TryTest {

    @Mock
    private MagicWizard wizard;

    @Before
    public void setUp() throws Exception {
        when(wizard.doMagicOrThrowException()).thenCallRealMethod();
    }

    @Test
    public void should_return_value_for_successful_method_call() throws Exception {
        // given
        when(wizard.doMagic()).thenReturn(DONE);

        // when
        Optional<MagicStatus> magicStatus = Try.it(wizard::doMagicOrThrowException).now();

        // then
        assertThat(magicStatus).isPresent().hasValue(DONE);
    }

    @Test
    public void should_swallow_exception_by_default_if_occured_and_return_empty() throws Exception {
        // given
        when(wizard.doMagic()).thenReturn(FAILED);

        // when
        Optional<MagicStatus> magicStatus = Try.it(wizard::doMagicOrThrowException).now();

        // then
        assertThat(magicStatus).isEmpty();
    }

    @Test
    public void should_return_value_for_successful_method_call_() throws Exception {
        // given
        when(wizard.doMagic()).thenReturn(DONE);

        // when
        MagicStatus magicStatus = Try.it(wizard::doMagicOrThrowException).orThrow();

        // then
        assertThat(magicStatus).isEqualTo(DONE);
    }

    @Test
    public void should_return_value_for_successful_method_call_with_specified_exception() throws Exception {
        // given
        when(wizard.doMagic()).thenReturn(DONE);

        // when
        MagicStatus magicStatus = Try.it(wizard::doMagicOrThrowException).orThrow(EmptyStackException::new);

        // then
        assertThat(magicStatus).isEqualTo(DONE);
    }

    @Test(expected = MagicException.class)
    public void should_throw_exception_if_occured() throws Exception {
        // given
        when(wizard.doMagic()).thenReturn(FAILED);

        // when
        Try.it(wizard::doMagicOrThrowException).orThrow();

        // then an exception should be thrown
    }

    @Test
    public void should_try_once_by_default() throws Exception {
        // given
        when(wizard.doMagic()).thenReturn(FAILED);

        // when
        try {
            Try.it(wizard::doMagicOrThrowException).orThrow();
        } catch (Exception ignored) {}

        // then
        verify(wizard, times(1)).doMagic();
    }

    @Test
    public void should_retry_configured_amount_of_times() throws Exception {
        // given
        when(wizard.doMagic()).thenReturn(FAILED);

        // when
        Try.it(wizard::doMagicOrThrowException).times(3).now();

        // then
        verify(wizard, times(3)).doMagic();
    }

    @Test
    public void should_execute_retries_with_delay_if_delay_configured() throws Exception {
        // given
        when(wizard.doMagic()).thenReturn(FAILED, FAILED, DONE);
        long startTime = System.currentTimeMillis();
        List<Long> retryTimes = new ArrayList<>();

        // when
        Try.it(wizard::doMagicOrThrowException).times(3)
                .onRetry(() -> retryTimes.add(System.currentTimeMillis()))
                .withDelay(Duration.ofMillis(100)).now();

        // then
        assertThat(retryTimes).hasSize(2);
        assertThat(retryTimes.get(0) - startTime).isGreaterThanOrEqualTo(100);
        assertThat(retryTimes.get(1) - retryTimes.get(0)).isGreaterThanOrEqualTo(100);
    }

    @Test
    public void should_fire_on_retry_callback() throws Exception {
        // given
        AtomicInteger callbacks = new AtomicInteger();
        when(wizard.doMagic()).thenReturn(FAILED);

        // when
        Try.it(wizard::doMagicOrThrowException).times(3).onRetry(callbacks::incrementAndGet).now();

        // then
        verify(wizard, times(3)).doMagic();
        assertThat(callbacks.get()).isEqualTo(2);
    }

    @Test
    public void should_fire_on_exception_callback() throws Exception {
        // given
        List<Exception> expected = new ArrayList<>();
        when(wizard.doMagic()).thenReturn(FAILED);

        // when
        Try.it(wizard::doMagicOrThrowException).times(3).onException(expected::add).now();

        // then
        verify(wizard, times(3)).doMagic();
        assertThat(expected).hasSize(1).hasOnlyElementsOfType(MagicException.class);
    }

    @Test
    public void should_throw_exception_of_caught_type() throws Exception {
        // given
        AtomicReference<Exception> caught = new AtomicReference<>();
        when(wizard.doMagic()).thenReturn(FAILED);

        // when
        try {
            Try.it(wizard::doMagicOrThrowException).orThrow();
        } catch (Exception ignored) {
            caught.set(ignored);
        }

        // then
        assertThat(caught.get()).isInstanceOf(MagicException.class);
    }

    @Test
    public void should_throw_exception_of_specified_type() throws Exception {
        // given
        AtomicReference<Exception> caught = new AtomicReference<>();
        when(wizard.doMagic()).thenReturn(FAILED);

        // when
        try {
            Try.it(wizard::doMagicOrThrowException).orThrow(EmptyStackException::new);
        } catch (Exception ignored) {
            caught.set(ignored);
        }

        // then
        assertThat(caught.get()).isInstanceOf(EmptyStackException.class);
    }

    @Test
    public void should_not_execute_method_for_zero_times_parameter() throws Exception {
        // given
        when(wizard.doMagic()).thenReturn(DONE);

        // when
        Optional<MagicStatus> magicStatus = Try.it(wizard::doMagicOrThrowException).times(0).now();

        // then
        verify(wizard, never()).doMagic();
        assertThat(magicStatus).isEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_exception_on_negative_times_parameter() throws Exception {
        // given
        when(wizard.doMagic()).thenReturn(DONE);

        // when
        Try.it(wizard::doMagicOrThrowException).times(-1).now();

        // then an exception should be thrown
    }

    @Test
    public void should_return_future() throws Exception {
        // given
        when(wizard.doMagic()).thenReturn(DONE);
        // when
        CompletableFuture<MagicStatus> future = Try.it(wizard::doMagicOrThrowException).future();
        // then
        assertThat(future.get()).isEqualTo(MagicStatus.DONE);
    }

    @Test
    public void should_be_able_to_cancel_future() throws Exception {
        // given
        when(wizard.doMagic()).thenAnswer(invocation -> {
            Thread.sleep(3000);
            return MagicStatus.DONE;
        });
        // when
        CompletableFuture<MagicStatus> future = Try.it(wizard::doMagicOrThrowException).future();
        future.cancel(true);
        // then
        assertThat(future).isCancelled();
    }

    @Test
    public void should_try_till_done_when_endless() throws Exception {
        // given
        when(wizard.doMagic()).thenReturn(FAILED, FAILED, FAILED, FAILED, DONE);

        // when
        CompletableFuture<MagicStatus> future = Try.it(wizard::doMagicOrThrowException)
                .onRetry(wizard::excuseForMagicFailure)
                .endless().future();
        MagicStatus magicStatus = future.get(5, TimeUnit.SECONDS);

        // then
        verify(wizard, times(5)).doMagic();
        verify(wizard, times(4)).excuseForMagicFailure();
        assertThat(magicStatus).isEqualTo(DONE);
    }

    @Test(expected = TimeoutException.class)
    public void should_throw_exception_when_endless_and_future_timed_out() throws Exception {
        // given
        when(wizard.doMagic()).thenReturn(FAILED);

        // when
        CompletableFuture<MagicStatus> future = Try.it(wizard::doMagicOrThrowException)
                .onRetry(wizard::excuseForMagicFailure)
                .endless().future();

        future.get(3, TimeUnit.SECONDS);

        // then
        // should throw exception
    }

    @Test
    public void should_execute_retries_with_delay_if_delay_configured_for_endless() throws Exception {
        // given
        when(wizard.doMagic()).thenReturn(FAILED, FAILED, DONE);
        long startTime = System.currentTimeMillis();
        List<Long> retryTimes = new ArrayList<>();

        // when
        CompletableFuture<MagicStatus> future = Try.it(wizard::doMagicOrThrowException)
                .endless()
                .onRetry(() -> retryTimes.add(System.currentTimeMillis()))
                .withDelay(Duration.ofMillis(100)).future();

        future.get(5, TimeUnit.SECONDS);
        // then
        assertThat(retryTimes).hasSize(2);
        assertThat(retryTimes.get(0) - startTime).isGreaterThanOrEqualTo(100);
        assertThat(retryTimes.get(1) - retryTimes.get(0)).isGreaterThanOrEqualTo(100);
    }

    @Test
    public void should_fire_on_exception_callback_for_endless() throws Exception {
        // given
        List<Exception> expected = new ArrayList<>();
        when(wizard.doMagic()).thenReturn(FAILED);

        // when
        CompletableFuture<MagicStatus> future = Try.it(wizard::doMagicOrThrowException)
                .endless()
                .onException(expected::add)
                .future();
        Thread.sleep(100);
        future.getNow(FAILED);

        // then
        verify(wizard, atLeastOnce()).doMagic();
        assertThat(expected).hasOnlyElementsOfType(MagicException.class);
    }
}