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
    
    public final void setDelegate(CasArray<T> delegate) {
        this.delegate = delegate;
    }

    public final CasArray<T> getDelegate() {
        return delegate;
    }

    public final void setThis(int idx, T toValue) {
        while(!super.cas(idx, toValue, get(idx)));
    }
}
