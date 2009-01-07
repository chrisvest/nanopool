package net.nanopool.contention;

/**
 * A ContentionHandler decides what to do when we reckon there's contention
 * on the pool.
 * Contention on the pool usually means that, last we checked, all connections
 * was in use, so this particular can not get a connection right away. This
 * means that we have wait a little while before we try again, and waiting
 * is exactly what ContentionHandler implementations do.
 * This interface is provided because the pool-using applications might want
 * to do logging or other stuff, while they wait.
 * The {@link NanoPoolDataSource} instances get their ContentionHandlers
 * through their constructors when they are created.
 * @author vest
 * @since 1.0
 */
public interface ContentionHandler {
    /**
     * Do something to ease contention on the pool.
     * This usually means calling {@link Thread#sleep(long)} or
     * {@link Thread#yield()}, but you could also throw a form of
     * {@link RuntimeException} if you'd rather give up on getting a connection
     * for this particular thread - just be sure to catch it in your own code!
     * @since 1.0
     */
    void handleContention();
}
