package it.podlodowski.javatry.util.retry;

import it.podlodowski.javatry.util.test.MagicException;
import it.podlodowski.javatry.util.test.MagicStatus;
import it.podlodowski.javatry.util.test.MagicWizard;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static it.podlodowski.javatry.util.test.MagicStatus.DONE;
import static it.podlodowski.javatry.util.test.MagicStatus.FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RetryTest {

    @Mock
    private MagicWizard wizard;

    @Before
    public void setUp() throws Exception {
        when(wizard.doMagicOrThrowException()).thenCallRealMethod();
    }

    @Test
    public void should_return_a_value_when_task_is_done_within_max_tries_bound() throws Exception {
        // given
        when(wizard.doMagic()).thenReturn(FAILED, FAILED, DONE);

        // when
        MagicStatus magicStatus =
                Retry.runWithRetry(wizard::doMagicOrThrowException, 3, wizard::excuseForMagicFailure);

        // then
        assertThat(magicStatus).isEqualTo(DONE);
    }

    @Test(expected = MagicException.class)
    public void should_throw_exception_when_task_is_not_done_within_max_tries_bound() throws Exception {
        // given
        when(wizard.doMagic()).thenReturn(FAILED, FAILED, FAILED);

        // when
        Retry.runWithRetry(wizard::doMagicOrThrowException, 3, wizard::excuseForMagicFailure);

        // then an exception should be thrown
    }

    @Test
    public void should_fire_callback_on_every_retry() throws Exception {
        // given
        when(wizard.doMagic()).thenReturn(FAILED, FAILED, DONE);

        // when
        Retry.runWithRetry(wizard::doMagicOrThrowException, 3, wizard::excuseForMagicFailure);

        // then
        verify(wizard, times(3)).doMagic();
        verify(wizard, times(2)).excuseForMagicFailure();
    }

    @Test
    public void should_not_fire_callback_on_last_retry() throws Exception {
        // given
        when(wizard.doMagic()).thenReturn(FAILED, FAILED, FAILED);

        // when
        try {
            Retry.runWithRetry(wizard::doMagicOrThrowException, 3, wizard::excuseForMagicFailure);
        } catch (Exception ignored) {}

        // then
        verify(wizard, times(3)).doMagic();
        verify(wizard, times(2)).excuseForMagicFailure();
    }

    @Test
    public void should_work_without_callback() throws Exception {
        // given
        when(wizard.doMagic()).thenReturn(FAILED, FAILED, DONE);

        // when
        Retry.runWithRetry(wizard::doMagicOrThrowException, 3);

        // then
        verify(wizard, times(3)).doMagic();
        verify(wizard, never()).excuseForMagicFailure();
    }

    @Test(expected = InstantiationException.class)
    public void should_not_be_possible_to_instantiate_static_class() throws Exception {
        // when
        new Retry();
        // then an exception should be thrown
    }
}