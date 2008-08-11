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
     */
    public NanoPoolRuntimeException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @see RuntimeException#RuntimeException(String, Throwable)
     * @param arg0
     * @param arg1
     */
    public NanoPoolRuntimeException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

    /**
     * @see RuntimeException#RuntimeException(String)
     * @param arg0
     */
    public NanoPoolRuntimeException(String arg0) {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    /**
     * @see RuntimeException#RuntimeException(Throwable)
     * @param arg0
     */
    public NanoPoolRuntimeException(Throwable arg0) {
        super(arg0);
    }
}
