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

/**
 * DOCUMENT ME!
 */
public class ITypeNeighbor<PropT> extends Pair<Guard<PropT>>
  implements Comparable<ITypeNeighbor<PropT>> {
  public ITypeNeighbor (int colorIn, Guard<PropT> transitionIn) {
    super(colorIn, transitionIn);
  }

  public void setColor (int colorIn) {
    super.setValue(colorIn);
  }

  public int getColor () {
    return super.getValue();
  }

  public void setTransition (Guard<PropT> transitionIn) {
    super.setElement(transitionIn);
  }

  public Guard<PropT> getTransition () {
    return super.getElement();
  }

  // Priority of comparison is made on the transition 
  public int compareTo (ITypeNeighbor<PropT> other) {
    int comparison = getTransition().compareTo(other.getTransition());

    if (comparison != 0)
      return comparison;
    return new Integer (getColor ()).compareTo (other.getColor ());
  }
  
  @SuppressWarnings ("unchecked")
  @Override
  public boolean equals (Object obj) {
    return obj != null && obj instanceof ITypeNeighbor<?> &&
      compareTo ((ITypeNeighbor<PropT>)obj) == 0;
  }
}
