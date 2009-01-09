package net.nanopool;

/**
 * A marker exception used for handling the mid-pool-resize special cases that
 * we have at various places, the JMX interface in particular.
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
