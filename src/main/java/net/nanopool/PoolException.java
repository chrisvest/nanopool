package net.nanopool;

public class PoolException extends RuntimeException {
    public PoolException() {
        super();
    }

    public PoolException(String message, Throwable cause) {
        super(message, cause);
    }

    public PoolException(String message) {
        super(message);
    }

    public PoolException(Throwable cause) {
        super(cause);
    }
}
