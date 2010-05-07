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

import java.util.List;


/**
 * DOCUMENT ME!
 */
public class SCCReduction {
  public static <PropT> Graph<PropT> reduce (Graph<PropT> g) {
    boolean changed;
    /* not needed? - pcd */
    String  type = g.getStringAttribute("type");
    String  ac = g.getStringAttribute("ac");
    assert ac.equals ("nodes") || ac.equals ("edges") :
      "invalid accepting type: " + ac;
    assert type.equals ("ba") || type.equals ("gba") :
      "invalid graph type: " + type;

    for (List<Node<PropT>> l: SCC.scc (g)) {
      clearExternalEdges(l, g);
    }

    do {
      changed = false;

      List<List<Node<PropT>>> sccs = SCC.scc(g);

      for (List<Node<PropT>> scc: sccs) {
        boolean accepting = isAccepting(scc, g);

        if (!accepting && isTerminal(scc)) {
          changed = true;

          for (Node<PropT> n: scc) {
            n.remove();
          }
        } else if (isTransient(scc) || !accepting) {
          changed |= anyAcceptingState(scc, g);
          clearAccepting(scc, g);
        }
      }
    } while (changed);

    return g;
  }

  /**
   * Check whether scc contains at least one node (if the graph is
   * node-accepting)/edge (otherwise) from every accepting set.
   * @param <PropT>
   * @param scc
   * @param g
   * @return
   */
  private static <PropT> boolean isAccepting (List<Node<PropT>> scc, Graph<PropT> g) {
    boolean gba = g.getStringAttribute ("type").equals ("gba"),
            nac = g.getStringAttribute ("ac").equals ("nodes");
    int    nsets = g.getIntAttribute("nsets");
    boolean found;

    if (!gba) {
      for (Node<PropT> n: scc)
        if (nac) {
          if (n.getBooleanAttribute("accepting"))
            return true;
        } else // edges
          for (Edge<PropT> e: n.getOutgoingEdges ())
            if (e.getBooleanAttribute("accepting"))
              return true;
      return false;
    } else { // gba
      for (int j = 0; j < nsets; j++) {
        found = false;
        nodes: for (Node<PropT> n: scc) {
          if (nac) {
            if (n.getBooleanAttribute("acc" + j)) {
              found = true;
              break nodes;
            }
          } else // edges
            for (Edge<PropT> e: n.getOutgoingEdges ()) {
              if (e.getBooleanAttribute("acc" + j)) {
                found = true;
                break nodes;
              }
            }
        }
        if (!found)
          return false;
      }
      return true;
    }
  }

  /**
   * Check whether scc has no edge into a node outside scc.
   * @param <PropT>
   * @param scc
   * @return
   */
  private static <PropT> boolean isTerminal (List<Node<PropT>> scc) {
    for (Node<PropT> n: scc) {
      for (Edge<PropT> e: n.getOutgoingEdges ()) {
        if (!scc.contains(e.getNext())) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * Check whether scc is a single node without loops.
   * @param <PropT>
   * @param scc
   * @return
   */
  private static <PropT> boolean isTransient (List<Node<PropT>> scc) {
    if (scc.size() != 1) {
      return false;
    }

    Node<PropT> n = scc.get(0);

    for (Edge<PropT> e: n.getOutgoingEdges ()) {
      if (e.getNext() == n) {
        return false;
      }
    }

    return true;
  }

  /**
   * Check whether any node (if the graph is node-accepting)
   * or edge (otherwise) in/from scc belongs to any accepting set.
   * @param <PropT>
   * @param scc
   * @param g
   * @return
   */
  private static <PropT> boolean anyAcceptingState (List<Node<PropT>> scc, Graph<PropT> g) {
    boolean gba = g.getStringAttribute ("type").equals ("gba"),
            nac = g.getStringAttribute ("ac").equals ("nodes");
    int nsets = g.getIntAttribute("nsets");

    for (Node<PropT> n: scc)
      if (!gba)
        if (nac) {
          if (n.getBooleanAttribute ("accepting"))
            return true;
        } else // edges
          for (Edge<PropT> e: n.getOutgoingEdges ())
            if (e.getBooleanAttribute("accepting"))
              return true;
      else // gba
        for (int k = 0; k < nsets; k++)
          if (nac) { 
            if (n.getBooleanAttribute ("acc" + k))
              return true;
          } else // edges
            for (Edge<PropT> f: n.getOutgoingEdges ())
              if (f.getBooleanAttribute("acc" + k))
                return true;
    return false;
  }

  /**
   * If the graph is node-accepting, remove every node of scc from
   * every accepting set; else, remove every edge with source in
   * scc from every accepting set.
   * @param <PropT>
   * @param scc
   * @param g
   */
  private static <PropT> void clearAccepting (List<Node<PropT>> scc, Graph<PropT> g) {
    boolean gba = g.getStringAttribute ("type").equals ("gba"),
            nac = g.getStringAttribute ("ac").equals ("nodes");
    int nsets = g.getIntAttribute("nsets");

    if (!gba)
      if (nac)
        for (Node<PropT> n: scc)
          n.setBooleanAttribute("accepting", false);
      else // edges
        for (Node<PropT> n: scc)
          for (Edge<PropT> e: n.getOutgoingEdges ())
            e.setBooleanAttribute("accepting", false);
    else // gba
      for (int j = 0; j < nsets; j++)
        if (nac)
          for (Node<PropT> n: scc)
            n.setBooleanAttribute("acc" + j, false);
        else // edges
          for (Node<PropT> n: scc)
            for (Edge<PropT> e: n.getOutgoingEdges ())
              e.setBooleanAttribute("acc" + j, false);
  }

  /**
   * If the graph is edge-accepting, remove every edge from scc
   * to a node outside scc from every accepting set.
   * @param <PropT>
   * @param scc
   * @param g
   */
  private static <PropT> void clearExternalEdges (List<Node<PropT>> scc, Graph<PropT> g) {
    int nsets = g.getIntAttribute("nsets");
    boolean gba = g.getStringAttribute ("type").equals ("gba"),
            nac = g.getStringAttribute ("ac").equals ("nodes");

    if (nac)
      return;
    for (Node<PropT> n: scc)
      for (Edge<PropT> e: n.getOutgoingEdges ())
        if (!scc.contains(e.getNext()))
          if (gba)
            for (int k = 0; k < nsets; k++)
              e.setBooleanAttribute("acc" + k, false);
          else // ba
            e.setBooleanAttribute ("accepting", false);
  }
}
