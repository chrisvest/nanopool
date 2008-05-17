package net.nanopool2.cas;

public interface CasArray<T> {
    boolean cas(int idx, T newValue, T oldValue);
    T get(int idx);
}
