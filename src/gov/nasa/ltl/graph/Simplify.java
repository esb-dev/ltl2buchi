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
public class Simplify {
  public static <PropT> Graph<PropT> simplify (Graph<PropT> g) {
    boolean simplified;

    do {
      simplified = false;
      for (Node<PropT> n0: g.getNodes ()) {
        for (Node<PropT> n1: g.getNodes ()) { // getNodes() copies list
          if (n1.getId() <= n0.getId())
            continue;
          if (n1.getBooleanAttribute("accepting") !=
              n0.getBooleanAttribute("accepting"))
            continue;
          if (equivalentCheck (n0, n1) && equivalentCheck (n1, n0)) {
            for (Edge<PropT> e: n1.getIncomingEdges ())
              new Edge<PropT>(e.getSource(), n0, e.getGuard(), e.getAction(), 
                       e.getAttributes());
            n1.remove();
            simplified = true;
          }
        }
      }
    } while (simplified);
    return g;
  }
  
  private static <PropT> boolean equivalentCheck (Node<PropT> n0,
                                                  Node<PropT> n1) {
    boolean equivalent = true;

    for (Edge<PropT> e0: n0.getOutgoingEdges ()) {
      if (!equivalent)
        return false;
      equivalent = false;
      for (Edge<PropT> e1: n1.getOutgoingEdges ())
        if ((e0.getNext() == e1.getNext()) || 
                ((e0.getNext() == n0 || e0.getNext() == n1) && 
                  (e1.getNext() == n0 || e1.getNext() == n1)))
          if (e0.getGuard().equals(e1.getGuard()) &&
              e0.getAction().equals(e1.getAction())) {
            equivalent = true;
            break;
          }
    }
    return equivalent;
  }
}
