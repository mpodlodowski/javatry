package it.podlodowski.javatry.util.precondition;

public class Preconditions {

    Preconditions() throws InstantiationException {
        throw new InstantiationException("Instances of this class are forbidden.");
    }

    public static void checkArgument(boolean expression, String errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
