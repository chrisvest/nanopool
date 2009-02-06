package net.nanopool;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.ConnectionPoolDataSource;
import net.nanopool.hooks.EventType;
import net.nanopool.hooks.Hook;
import org.junit.Test;

/**
 *
 * @author cvh
 */
public class HooksTest extends NanoPoolTestBase {
    private final AtomicInteger connectAttempts = new AtomicInteger();
    private final AtomicInteger preConnectCheck = new AtomicInteger();
    private final AtomicInteger postConnectCheck = new AtomicInteger();
    private final AtomicInteger preReleaseCheck = new AtomicInteger();
    private final AtomicInteger postReleaseCheck = new AtomicInteger();

    static class SyncHook implements Hook {
        private final String name;
        private final AtomicInteger before;
        private final AtomicInteger after;

        SyncHook(String name, AtomicInteger before, AtomicInteger after) {
            this.name = name;
            this.before = before;
            this.after = after;
        }

        public void run(EventType type, ConnectionPoolDataSource source, Connection con, SQLException sqle) {
            assertEquals(name + " hook out of sync.",
                    before.get(), after.incrementAndGet());
        }
    }

    @Override
    protected Configuration buildConfig() {
        return super.buildConfig()
                .addPreConnectHook(new SyncHook("pre connect", connectAttempts, preConnectCheck))
                .addPostConnectHook(new SyncHook("post connect", preConnectCheck, postConnectCheck))
                .addPreReleaseHook(new SyncHook("pre release", postConnectCheck, preReleaseCheck))
                .addPostReleaseHook(new SyncHook("post release", preReleaseCheck, postReleaseCheck));
    }

    @Test
    public void hooksMustRunInCorrectOrder() throws SQLException {
        pool = npds();
        assertCorrectHooksSequence(pool);
        assertCorrectHooksSequence(pool);
        assertCorrectHooksSequence(pool);
        assertCorrectHooksSequence(pool);
    }

    private void assertCorrectHooksSequence(NanoPoolDataSource pool) throws SQLException {
        int count = connectAttempts.incrementAndGet();
        Connection con = pool.getConnection();
        con.close();
        assertEquals(count, postReleaseCheck.get());
    }
}
