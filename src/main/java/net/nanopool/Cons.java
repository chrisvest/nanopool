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
