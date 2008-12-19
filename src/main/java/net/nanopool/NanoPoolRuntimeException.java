package net.nanopool;

/**
 * Thrown under the same premises as any other {@link RuntimeException}.
 * That is, whenever something unanticipated went wrong.
 * @author vest
 * @since 1.0
 */
public class NanoPoolRuntimeException extends RuntimeException {
    private static final long serialVersionUID = -2359233167832636507L;

    /**
     * @see RuntimeException#RuntimeException()
     * @since 1.0
     */
    public NanoPoolRuntimeException() {
        super();
    }

    /**
     * @see RuntimeException#RuntimeException(String, Throwable)
     * @param msg
     * @param th
     * @since 1.0
     */
    public NanoPoolRuntimeException(String msg, Throwable th) {
        super(msg, th);
    }

    /**
     * @see RuntimeException#RuntimeException(String)
     * @param msg
     * @since 1.0
     */
    public NanoPoolRuntimeException(String msg) {
        super(msg);
    }

    /**
     * @see RuntimeException#RuntimeException(Throwable)
     * @param th
     * @since 1.0
     */
    public NanoPoolRuntimeException(Throwable th) {
        super(th);
    }
}
