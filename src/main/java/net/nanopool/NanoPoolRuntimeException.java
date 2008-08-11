package net.nanopool;

/**
 * Thrown under the same premises as any other {@link RuntimeException}.
 * That is, whenever something unanticipated went wrong.
 * @author vest
 * @since 1.0
 */
public class NanoPoolRuntimeException extends RuntimeException {
    /**
     * @see RuntimeException#RuntimeException()
     * @since 1.0
     */
    public NanoPoolRuntimeException() {
        super();
    }

    /**
     * @see RuntimeException#RuntimeException(String, Throwable)
     * @param arg0
     * @param arg1
     * @since 1.0
     */
    public NanoPoolRuntimeException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    /**
     * @see RuntimeException#RuntimeException(String)
     * @param arg0
     * @since 1.0
     */
    public NanoPoolRuntimeException(String arg0) {
        super(arg0);
    }

    /**
     * @see RuntimeException#RuntimeException(Throwable)
     * @param arg0
     * @since 1.0
     */
    public NanoPoolRuntimeException(Throwable arg0) {
        super(arg0);
    }
}
