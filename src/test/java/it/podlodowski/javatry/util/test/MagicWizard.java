package it.podlodowski.javatry.util.test;

public class MagicWizard {
    public MagicStatus doMagic() {
        throw new IllegalStateException("This method should be mocked");
    }

    public MagicStatus doMagicOrThrowException() throws MagicException {
        MagicStatus status = doMagic();
        if (status == MagicStatus.FAILED) {
            throw new MagicException();
        }
        return status;
    }

    public void excuseForMagicFailure() {
        System.out.println("Oops! Sorry for my magic fail.");
    }
}
