//
// Copyright (C) 2006 United States Government as represented by the
// Administrator of the National Aeronautics and Space Administration
// (NASA).  All Rights Reserved.
// 
// This software is distributed under the NASA Open Source Agreement
// (NOSA), version 1.3.  The NOSA has been approved by the Open Source
// Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
// directory tree for the complete NOSA document.
// 
// THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
// KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
// LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
// SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
// A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
// THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
// DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//
package gov.nasa.ltl.graph;

import java.util.Hashtable;
import java.util.Map;


/**
 * DOCUMENT ME!
 */
public class Attributes {
  private Hashtable<Object, String> ht;

  public Attributes () {
    ht = new Hashtable<Object, String>();
  }

  public Attributes (Attributes a) {
    ht = new Hashtable<Object, String>(a.ht);
  }

  public void setBoolean (String name, boolean value) {
    if (value) {
      ht.put(name, "");
    } else {
      ht.remove(name);
    }
  }

  public boolean getBoolean (String name) {
    return ht.get(name) != null;
  }

  public void setInt (String name, int value) {
    ht.put(name, Integer.toString(value));
  }

  public int getInt (String name) {
    Object o = ht.get(name);

    if (o == null) {
      return 0;
    }

    try {
      return Integer.parseInt((String) o);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  public void setString (String name, String value) {
    ht.put(name, value);
  }

  public String getString (String name) {
    return ht.get(name);
  }

  public void unset (String name) {
    ht.remove(name);
  }
  
  public Map<Object, String> getAll () {
    return new Hashtable<Object, String> (ht);
  }
}
