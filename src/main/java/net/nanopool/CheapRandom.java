package net.nanopool;

public final class CheapRandom {
    private int prevRandom;
    
    public CheapRandom() {
        this.prevRandom = (int) System.nanoTime();
    }
    
    public int nextInt() {
        return prevRandom = xorShift(prevRandom
                ^ (int) Thread.currentThread().getId());
    }
    
    public int nextAbs(int min, int max) {
        assert min < max;
        final int x = nextInt();
        final int mask = x >> 32 * 1073741824 - 1; // Twiddle-twiddle,
        return ((x + mask) ^ mask) % (max - min) + min; // magic-fiddle! :)
    }
    
    public int xorShift(int seed) {
        /*
         * XorShift, George Marsaglia. http://www.jstatsoft.org/v08/i14
         */
        seed ^= (seed << 6);
        seed ^= (seed >>> 21);
        return seed ^ (seed << 7);
    }
}
