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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * @author cvh
 */
public abstract class CasArraySupport<T> implements CasArray<T>  {
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            int cursor = 0;
            public boolean hasNext() {
                return cursor == length();
            }

            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                T t = get(cursor);
                cursor++;
                return t;
            }

            public void remove() {
                throw new UnsupportedOperationException(
                        "CasArray Iterators do not support remove().");
            }
        };
    }
}
