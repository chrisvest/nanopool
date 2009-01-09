package net.nanopool;

/**
 *
 * @author cvh
 */
class CasArrayOutdatedException extends NanoPoolRuntimeException {
    static final CasArrayOutdatedException INSTANCE =
            new CasArrayOutdatedException();

    private CasArrayOutdatedException() {
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
