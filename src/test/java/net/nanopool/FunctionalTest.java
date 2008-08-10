package net.nanopool;

import junit.framework.TestSuite;
import net.nanopool.cas.CasArray;
import net.nanopool.cas.StrongAtomicCasArray;
import net.nanopool.cas.StrongStripedAtomicCasArray;
import net.nanopool.cas.UnsafeCasArray;
import net.nanopool.cas.WeakAtomicCasArray;
import net.nanopool.cas.WeakStripedAtomicCasArray;

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
        Factory<CasArray<Connector>>[] casArrayFactories = new Factory[] {
            new Factory<CasArray<Connector>>() {
                public CasArray<Connector> create() {
                    return new StrongAtomicCasArray<Connector>(CA_SIZE);
                }
            },
            new Factory<CasArray<Connector>>() {
                public CasArray<Connector> create() {
                    return new StrongStripedAtomicCasArray<Connector>(CA_SIZE);
                }
            },
            new Factory<CasArray<Connector>>() {
                public CasArray<Connector> create() {
                    return new WeakAtomicCasArray<Connector>(CA_SIZE);
                }
            },
            new Factory<CasArray<Connector>>() {
                public CasArray<Connector> create() {
                    return new WeakStripedAtomicCasArray<Connector>(CA_SIZE);
                }
            },
            new Factory<CasArray<Connector>>() {
                public CasArray<Connector> create() {
                    return new UnsafeCasArray(CA_SIZE);
                }
            },
        };
    }
}
