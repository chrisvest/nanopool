package net.nanopool.cas;

public interface CasArray<T> {
    boolean cas(int idx, T newValue, T oldValue);
    T get(int idx);
    int length();
}
