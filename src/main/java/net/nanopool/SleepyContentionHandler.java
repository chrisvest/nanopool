package net.nanopool;

public class SleepyContentionHandler implements ContentionHandler {
    public void handleContention() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
