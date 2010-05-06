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
public class SuperSetReduction {
  @SuppressWarnings ("unchecked")
  public static <PropT> Graph<PropT> reduce (Graph<PropT> g) {
    final int nsets = g.getIntAttribute("nsets");
    String    type = g.getStringAttribute("type");
    String    ac = g.getStringAttribute("ac");
    int nsomething;
    Edge<PropT>[] edges = null;
    boolean[][] asets;

    if (!type.equals("gba")) {
      throw new RuntimeException("invalid graph type: " + type);
    }
    assert ac.equals ("nodes") || ac.equals ("edges") :
      "invalid accepting type: " + ac;
    if (ac.equals("nodes")) {
      final int         nnodes = g.getNodeCount();
      nsomething = nnodes;
      final boolean[][] asets_ = new boolean[nsets][nnodes];
      asets = asets_;
      g.forAllNodes(new EmptyVisitor<PropT>() {
        public void visitNode (Node<PropT> n) {
          for (int i = 0; i < nsets; i++) {
            String acc = "acc" + i;

            if (n.getBooleanAttribute(acc)) {
              asets_[i][n.getId()] = true;
              n.setBooleanAttribute(acc, false);
            }
          }
        }
      });
    } else { // edges
      final int         nedges = g.getEdgeCount();
      nsomething = nedges;
      final boolean[][] asets_ = new boolean[nsets][nedges];
      asets = asets_;
      final Edge<PropT>[] edges_ = new Edge[nedges];
      edges = edges_;
      g.forAllEdges(new EmptyVisitor<PropT>(new Integer(0)) {
        public void visitEdge (Edge<PropT> e) {
          int id = ((Integer) arg).intValue();
          arg = new Integer(id + 1);

          edges_[id] = e;

          for (int i = 0; i < nsets; i++) {
            String acc = "acc" + i;

            if (e.getBooleanAttribute(acc)) {
              asets_[i][id] = true;
              e.setBooleanAttribute(acc, false);
            }
          }
        }
      });
    }

    boolean[] remove = new boolean[nsets];

    for (int i = 0; i < nsets; i++) {
      for (int j = 0; (j < nsets) && !remove[i]; j++) {
        if ((i != j) && !remove[j]) {
          if (included(asets[j], asets[i])) {
            remove[i] = true;
          }
        }
      }
    }

    int n_nsets = 0;

    for (int i = 0; i < nsets; i++) {
      if (!remove[i]) {
        n_nsets++;
      }
    }

    boolean[][] n_asets = new boolean[n_nsets][nsomething];

    n_nsets = 0;

    for (int i = 0; i < nsets; i++) {
      if (!remove[i]) {
        n_asets[n_nsets++] = asets[i];
      }
    }

    g.setIntAttribute("nsets", n_nsets);

    if (ac.equals ("nodes")) {
      for (int i = 0; i < nsomething; i++) {
        Node<PropT> n = g.getNode(i);

        for (int j = 0; j < n_nsets; j++) {
          if (n_asets[j][i]) {
            n.setBooleanAttribute("acc" + j, true);
          }
        }
      }
    } else { // edges
      for (int i = 0; i < nsomething; i++) {
        Edge<PropT> e = edges[i];

        for (int j = 0; j < n_nsets; j++) {
          if (n_asets[j][i]) {
            e.setBooleanAttribute("acc" + j, true);
          }
        }
      }
    }
    return g;
  }

  private static boolean included (boolean[] a, boolean[] b) {
    final int al = a.length;
    final int bl = b.length;

    if (bl < al) {
      throw new IllegalArgumentException("Second array cannot be smaller!");
    }
    
    for (int i = 0; i < al; i++) {
      if (a[i] && !b[i]) {
        return false;
      }
    }

    return true;
  }
}
