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
    
    public static void main(String[] args) {
        runTest();
        runTest();
        runTest();
        runTest();
        runTest();
        runTest();
        runTest();
    }
    
    private static void runTest() {
        long start = System.nanoTime();
        CheapRandom rand = new CheapRandom();
        // java.util.Random rand = new java.util.Random();
        int r = 0;
        for (int i = 0; i < 1000000; i++) {
            r = rand.nextInt();
        }
        long end = System.nanoTime();
        long elapsed = end - start;
        System.out.printf("Time used: %s ms. r = %s\n", elapsed / 1000000.0, r);
    }
}
