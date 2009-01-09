package net.nanopool.cas;

public abstract class ResizableAtomicCasArraySupport<T>
        extends AtomicCasArraySupport<T> implements ResizableCasArray<T> {
    private volatile CasArray<T> delegate;
    
    public ResizableAtomicCasArraySupport(int size) {
        super(size);
    }
    
    @Override
    public final boolean cas(int idx, T newValue, T oldValue) {
        CasArray<T> theDelegate = delegate;
        if (theDelegate != null) {
            return delegate.cas(idx, newValue, oldValue);
        }
        return super.cas(idx, newValue, oldValue);
    }
    
    public final void setCasDelegate(CasArray<T> delegate) {
        this.delegate = delegate;
    }
}
