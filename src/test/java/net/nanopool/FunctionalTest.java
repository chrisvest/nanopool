/*
   Copyright 2008-2009 Christian Vest Hansen

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package net.nanopool;

import net.nanopool.contention.DefaultContentionHandler;
import net.nanopool.contention.ContentionHandler;
import net.nanopool.contention.SleepyContentionHandler;
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
