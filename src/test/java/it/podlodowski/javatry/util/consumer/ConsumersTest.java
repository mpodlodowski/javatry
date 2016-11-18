package it.podlodowski.javatry.util.consumer;

import it.podlodowski.javatry.util.consumer.Consumers;
import org.junit.Test;

public class ConsumersTest {
    @Test(expected = InstantiationException.class)
    public void should_not_be_possible_to_instantiate_helper_class() throws Exception {
        // when
        new Consumers();
        // then an exception should be thrown
    }
}