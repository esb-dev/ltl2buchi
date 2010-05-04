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

import java.util.Iterator;
import java.util.TreeSet;


/**
 * DOCUMENT ME!
 */
public class ColorPair<PropT> extends Pair<TreeSet<ITypeNeighbor<PropT>>>
  implements Comparable<ColorPair<PropT>> {
  public ColorPair (int colorIn, TreeSet<ITypeNeighbor<PropT>> iMaxSetIn) {
    super(colorIn, iMaxSetIn);
  }

  public void setColor (int colorIn) {
    super.setValue(colorIn);
  }

  public int getColor () {
    return super.getValue();
  }

  public void setIMaxSet (TreeSet<ITypeNeighbor<PropT>> iMaxSetIn) {
    super.setElement(iMaxSetIn);
  }

  public TreeSet<ITypeNeighbor<PropT>> getIMaxSet () {
    return super.getElement();
  }

  public int compareTo (ColorPair<PropT> other) {
    TreeSet<ITypeNeighbor<PropT>>   otherSet = other.getIMaxSet();

    if (getIMaxSet().size() < otherSet.size()) {
      return -1;
    }

    if (getIMaxSet().size() > otherSet.size()) {
      return 1;
    }

    // TreeSets are ordered !!
    Iterator<ITypeNeighbor<PropT>> otherIter = otherSet.iterator ();
    for (ITypeNeighbor<PropT> currNeigh: getIMaxSet()) {
      assert otherIter.hasNext ();
      int comparison = currNeigh.compareTo(otherIter.next ());

      if ((comparison < 0) || (comparison > 0)) {
        return comparison;
      }
    }

    if (getColor() < other.getColor()) {
      return -1;
    }

    if (getColor() > other.getColor()) {
      return 1;
    }

    return 0;
  }

  @SuppressWarnings ("unchecked")
  public boolean equals (Object o) {
    if (o == null || !(o instanceof ColorPair<?>))
      return false;
    ColorPair<PropT> other = (ColorPair<PropT>) o;
    return compareTo(other) == 0;
  }
  
  public int hashCode() {
    assert false : "hashCode not designed";
    return 42; // any arbitrary constant will do
    // thanks, FindBugs!
  }
}
