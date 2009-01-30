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

/**
 * A medium-quality pseudo-random number generator.
 * The implementation is based on George Marsaglias
 * <a href="http://www.jstatsoft.org/v08/i14">XorShift</a> code, and is
 * implemented in such a way, that it is extremely fast and does not require
 * any form of external or internal synchronization in order to be thread-safe.
 * In other words, if you're fine with the somewhat lower quality, you will
 * find it running circles around {@link java.util.Random} - especially in the
 * face of concurrent usage.
 * @author vest
 * @since 1.0
 */
public final class CheapRandom {
    private int prevRandom;
    
    /**
     * Construct a new CheapRandom instance.
     * @since 1.0
     */
    public CheapRandom() {
        this.prevRandom = (int) System.nanoTime();
    }
    
    /**
     * Compute the next pseudo-random integer from the current seed-state.
     * @return A pseudo-random integer.
     * @since 1.0
     */
    public int nextInt() {
        // Using the current thread-id as part of the prime for the XorShift is
        // the reason we can stay thread-safe without synchronization or
        // CAS'ing of any kind.
        // So... don't remove it, m'kay?
        return prevRandom = xorShift(prevRandom
                ^ (int) Thread.currentThread().getId());
    }
    
    /**
     * Compute the next pseudo-random integer that is greater than or equal to
     * 'min', and less than 'max'.
     * @param min Produce only values greater than or equal to this value.
     * @param max Produce only values less than this value.
     * @return A pesudo-random integer within the specified range.
     * @since 1.0
     */
    public int nextAbs(int min, int max) {
        assert min < max;
        final int x = nextInt();
        final int mask = x >> 32 * 1073741824 - 1; // Twiddle-twiddle,
        return ((x + mask) ^ mask) % (max - min) + min; // magic-fiddle! :)
    }
    
    /**
     * Perform a XorShift spread operation on the provided seed value, and
     * return the result.
     * This method is side-effect free (pure) and fully deterministic, and
     * does not operate on any global or otherwise shared state.
     * @param seed The input to be XorShifted
     * @return The result of XorShifting the seed value.
     * @since 1.0
     */
    public static int xorShift(int seed) {
        // XorShift, George Marsaglia. http://www.jstatsoft.org/v08/i14
        seed ^= (seed << 6);
        seed ^= (seed >>> 21);
        return seed ^ (seed << 7);
    }
}
