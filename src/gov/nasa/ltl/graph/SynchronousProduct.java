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
public class SynchronousProduct {
  public static <PropT1, PropT2> void dfs (Graph<PropT1> g,
      Node<PropT1>[][] nodes, int nsets, Node<PropT1> n0, Node<PropT2> n1) {
    Node<PropT1> n = get(g, nodes, n0, n1);

    for (Edge<PropT1> e0: n0.getOutgoingEdges ()) {
      Node<PropT1>    next0 = e0.getNext();
      Edge<PropT2>    theEdge = null;

      boolean found = false;

      for (Edge<PropT2> e1: n1.getOutgoingEdges ()) {
        if (found)
          break;
        
        if (e1.getBooleanAttribute("else")) {
          if (theEdge == null) {
            theEdge = e1;
          }
        } else {
          found = true;

          for (int i = 0; i < nsets; i++) {
            boolean b0 = e0.getBooleanAttribute("acc" + i);
            boolean b1 = e1.getBooleanAttribute("acc" + i);

            if (b1 && !b0) { // corrected by Dimitra
              found = false;

              break;
            }
          }
        }

        if (found) {
          theEdge = e1;
        }
      }

      if (theEdge != null) {
        Node<PropT2> next1 = theEdge.getNext();
        boolean newNext = isNew(nodes, next0, next1);
        Node<PropT1> next = get(g, nodes, next0, next1);
        // for side-effect in constructor:
        new Edge<PropT1>(n, next, e0.getGuard(), theEdge.getAction(), null);

        if (newNext) {
          dfs(g, nodes, nsets, next0, next1);
        }
      }
    }
  }

  @SuppressWarnings ("unchecked")
  public static <PropT1, PropT2> Graph<PropT1> product (Graph<PropT1> g0,
                                                        Graph<PropT2> g1) {
    int nsets = g0.getIntAttribute("nsets");

    if (nsets != g1.getIntAttribute("nsets")) {
      System.err.println("Different number of accepting sets");
      System.exit(1);
    }

    Node<PropT1>[][] nodes;
    Graph<PropT1>    g = new Graph<PropT1>();
    g.setStringAttribute("type", "ba");
    g.setStringAttribute("ac", "nodes");

    nodes = (Node<PropT1>[][])new Node[g0.getNodeCount()][g1.getNodeCount()];

    dfs(g, nodes, nsets, g0.getInit(), g1.getInit());

    return g;
  }

  private static <PropT1, PropT2> boolean isNew (Node<PropT1>[][] nodes,
      Node<PropT1> n0, Node<PropT2> n1) {
    return nodes[n0.getId()][n1.getId()] == null;
  }

  private static <PropT1, PropT2> Node<PropT1> get (Graph<PropT1> g,
      Node<PropT1>[][] nodes, Node<PropT1> n0, Node<PropT2> n1) {
    if (nodes[n0.getId()][n1.getId()] == null) {
      Node<PropT1> n = new Node<PropT1>(g);
      String label0 = n0.getStringAttribute("label");
      String label1 = n1.getStringAttribute("label");

      if (label0 == null) {
        label0 = Integer.toString(n0.getId());
      }

      if (label1 == null) {
        label1 = Integer.toString(n1.getId());
      }

      n.setStringAttribute("label", label0 + "+" + label1);

      if (n1.getBooleanAttribute("accepting")) {
        n.setBooleanAttribute("accepting", true);
      }

      return nodes[n0.getId()][n1.getId()] = n;
    }

    return nodes[n0.getId()][n1.getId()];
  }
}
