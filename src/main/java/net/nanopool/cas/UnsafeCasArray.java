/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.nanopool.cas;

import java.lang.reflect.Field;

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
    
    public UnsafeCasArray(int poolSize) {
        if (poolSize == 0)
            throw new IllegalArgumentException(
                    "Pool size must be greater than 0.");
        try {
            array = (T[]) new Object[poolSize];
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
        UnsafeCasArray uca = new UnsafeCasArray(5);
        System.out.println("array base offset: " + uca.theUnsafe.arrayBaseOffset(Object[].class));
        System.out.println("array index scale: " + uca.theUnsafe.arrayIndexScale(Object[].class));
    }
}
