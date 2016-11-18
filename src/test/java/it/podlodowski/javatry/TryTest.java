package it.podlodowski.javatry;

import it.podlodowski.javatry.util.test.MagicException;
import it.podlodowski.javatry.util.test.MagicStatus;
import it.podlodowski.javatry.util.test.MagicWizard;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
}