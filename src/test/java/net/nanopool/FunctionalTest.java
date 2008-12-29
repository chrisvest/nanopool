package net.nanopool;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import junit.framework.TestSuite;
import net.nanopool.cas.CasArray;
import net.nanopool.cas.StrongAtomicCasArray;
import net.nanopool.cas.StrongStripedAtomicCasArray;
import net.nanopool.cas.WeakAtomicCasArray;
import net.nanopool.cas.WeakStripedAtomicCasArray;

import org.junit.Test;

public class FunctionalTest extends TestSuite {
    protected static final int CA_SIZE = 10;

    public FunctionalTest() {
        super("NanoPool Functionality Test Suite");
        Factory<ContentionHandler>[] contentionHandlerFactories = new Factory[] {
            new Factory<ContentionHandler>() {
                public ContentionHandler create() {
                    return new DefaultContentionHandler();
                }
            },
            new Factory<ContentionHandler>() {
                public ContentionHandler create() {
                    return new SleepyContentionHandler();
                }
            }
        };
        Factory<CasArray>[] casArrayFactories = new Factory[] {
            new Factory<CasArray>() {
                public CasArray create() {
                    return new StrongAtomicCasArray(CA_SIZE);
                }
            },
            new Factory<CasArray>() {
                public CasArray create() {
                    return new StrongStripedAtomicCasArray(CA_SIZE);
                }
            },
            new Factory<CasArray>() {
                public CasArray create() {
                    return new WeakAtomicCasArray(CA_SIZE);
                }
            },
            new Factory<CasArray>() {
                public CasArray create() {
                    return new WeakStripedAtomicCasArray(CA_SIZE);
                }
            },
        };
    }
    
    @Test
    public void metaTest() {
        assertThat(true, is(true));
    }
}
