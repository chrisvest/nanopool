package net.nanopool;

/**
 * An implementation of {@link ContentionHandler} that throws a
 * {@link NanoPoolRuntimeException} when there's too much contention on the
 * pool.
 * @author cvh
 * @since 1.0
 */
public class ThrowingContentionHandler implements ContentionHandler {
    public void handleContention() {
        throw new NanoPoolRuntimeException("Connection pool contion too high.");
    }
}
