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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

final class Cons<T> {

    final T first;
    final Cons<T> rest;

    Cons(T f, Cons<T> r) {
        if (f == null)
            throw new NullPointerException("First of a Cons cannot be null.");
        this.first = f;
        this.rest = r;
    }

    private void appendTo(Collection<T> coll) {
        Cons<T> c = this;
        while (c != null) {
            coll.add(c.first);
            c = c.rest;
        }
    }

    List<T> toList() {
        List<T> list = new ArrayList();
        appendTo(list);
        return Collections.unmodifiableList(list);
    }

    boolean contains(T t) {
        Cons<T> c = this;
        while (c != null) {
            if (c.first.equals(t)) return true;
            c = c.rest;
        }
        return false;
    }
}
