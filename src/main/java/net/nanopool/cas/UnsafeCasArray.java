package net.nanopool.cas;

import java.lang.reflect.Field;

import net.nanopool.CheapRandom;
import sun.misc.Unsafe;

/**
 *
 * @author vest
 */
public class UnsafeCasArray<T> implements CasArray<T> {
    private final Unsafe theUnsafe;
    private final int offset;
    private final int scale;
    private final T[] array;
    
    public UnsafeCasArray(int size) {
        if (size == 0)
            throw new IllegalArgumentException(
                    "Pool size must be greater than 0.");
        try {
            array = (T[]) new Object[size];
            Class<Unsafe> unsafeClass = Unsafe.class;
            Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            theUnsafe = (Unsafe)unsafeField.get(unsafeClass);
            offset = theUnsafe.arrayBaseOffset(Object[].class);
            scale = theUnsafe.arrayIndexScale(Object[].class);
            /*
             * Ensure JMM visibility of 'array'.
             * Do we *really* need this? AFAIK setting a final field
             * happens-before the ctor returns, and would this not be
             * anough to ensure visibility of 'array'? Cliff Click of
             * high-scale-lib thinks so in his NonBlockingHashMap impl.
             * but Doug Lea seems to disagree in his AtomicReferenceArray.
             * What gives??
             */
            theUnsafe.putObjectVolatile(array, idx(0), null);
        } catch (Exception ex) {
            throw new UnsupportedOperationException(
                    "UnsafeCasArray probably not supported on your JVM.", ex);
        }
    }
    
    private final int idx(int i) {
        return offset + i * scale;
    }

    public boolean cas(int idx, T newValue, T oldValue) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public T get(int idx) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int length() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public static void main(String[] args) {
//        UnsafeCasArray uca = new UnsafeCasArray(5);
//        System.out.println("array base offset: " + uca.theUnsafe.arrayBaseOffset(Object[].class));
//        System.out.println("array index scale: " + uca.theUnsafe.arrayIndexScale(Object[].class));
        
        CheapRandom r = new CheapRandom();
        int keep = 1234567890;
        long start = System.nanoTime();
        for (int i = 0; i < 1000000000; i++) {
            keep ^= abs(r.nextInt());
//            keep ^= Math.abs(r.nextInt());
        }
        long end = System.nanoTime();
        long elapsed = end - start;
        System.out.printf("Time used: %s ms. keep = %s\n", elapsed / 1000000.0, keep);
        
    }
    
    private static int abs(int x) {
        final int mask = x >> 32 * 1073741824 - 1;
        return (x + mask) ^ mask;
    }
}
