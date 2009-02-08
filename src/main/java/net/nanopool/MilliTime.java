package net.nanopool;

/**
 * A {@link TimeSource} implementation based on {@link System#currentTimeMillis()}.
 * @author cvh
 */
public class MilliTime implements TimeSource {
    public static final MilliTime INSTANCE = new MilliTime();

    public long millisecondsToUnit(long millis) {
        return millis;
    }

    public long now() {
        return System.currentTimeMillis();
    }
}
