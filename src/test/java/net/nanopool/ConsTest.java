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

import java.util.List;
import java.util.ListIterator;
import static org.junit.Assert.*;

import org.junit.Test;

public class ConsTest {
  private static final Object x = new Object();
  private static final Object y = new Object();
  private static final Object z = new Object();
  
  @Test
  public void testProperNormalConstruction() {
    Cons c = new Cons(x, null);
    assertSame(x, c.first);
    assertNull(c.rest);
  }
  
  @Test
  public void testConsesCannotContainNullElements() {
    try {
      new Cons(null, null);
      fail("A Cons with a null 'first' was allowed to be cnstructed.");
    } catch (NullPointerException _) {
      // wo-hoo.
    }
  }
  
  @Test
  public void testToListIsInCorrectOrder() {
    Cons c = new Cons(y, null);
    c = new Cons(x, c);
    List l = c.toList();
    assertSame(x, l.get(0));
    assertSame(y, l.get(1));
  }
  
  @Test
  public void testToListIsUnmodifiable() {
    Cons c = new Cons(y, null);
    c = new Cons(x, c);
    List l = c.toList();
    try {
      l.add(z);
      fail("list allowed add()");
    } catch (RuntimeException _) {
      // wo-hoo
    }
    try {
      l.remove(x);
      fail("list allowed remove(Object)");
    } catch (RuntimeException _) {
      // wo-hoo
    }
    try {
      l.remove(0);
      fail("list allowed remove(index)");
    } catch (RuntimeException _) {
      // wo-hoo
    }
    try {
      l.clear();
      fail("list allowed clear()");
    } catch (RuntimeException _) {
      // wo-hoo
    }
    try {
      ListIterator li = l.listIterator();
      li.next();
      li.remove();
      fail("list allowed ListIterator.remove()");
    } catch (RuntimeException _) {
      // wo-hoo
    }
  }
  
  @Test
  public void testContains() {
    Cons c = new Cons(y, null);
    c = new Cons(x, c);
    assertTrue(c.contains(x));
    assertTrue(c.contains(y));
    assertFalse(c.contains(z));
  }
}
