package it.podlodowski.javatry.util.consumer;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Consumers {

    Consumers() throws InstantiationException {
        throw new InstantiationException("Instances of this class are forbidden.");
    }

    public static <T> Consumer<T> doNothing() {
        return t -> {};
    }
}
