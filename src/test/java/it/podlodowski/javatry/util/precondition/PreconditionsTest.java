package it.podlodowski.javatry.util.precondition;

import it.podlodowski.javatry.util.precondition.Preconditions;
import org.junit.Test;

public class PreconditionsTest {
    @Test(expected = InstantiationException.class)
    public void should_not_be_possible_to_instantiate_helper_class() throws Exception {
        // when
        new Preconditions();
        // then an exception should be thrown
    }
}