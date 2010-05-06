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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


/**
 * DOCUMENT ME!
 */
public class SCC {
  @SuppressWarnings ("unchecked")
  public static <PropT> List<List<Node<PropT>>> scc (Graph<PropT> g) {
    Node<PropT> init = g.getInit();

    if (init == null) {
      return new LinkedList<List<Node<PropT>>>();
    }

    init.setBooleanAttribute("_reached", true);

    SCCState<PropT> s = new SCCState<PropT>();
    visit(init, s);

    final List<Node<PropT>>[] scc = new List[s.SCC];

    for (int i = 0; i < s.SCC; i++) {
      scc[i] = new LinkedList<Node<PropT>>();
    }

    g.forAllNodes(new EmptyVisitor<PropT>() {
      public void visitNode (Node<PropT> n) {
        scc[n.getIntAttribute("_scc")].add(n);

        n.setBooleanAttribute("_reached", false);
        n.setBooleanAttribute("_dfsnum", false);
        n.setBooleanAttribute("_low", false);
        n.setBooleanAttribute("_scc", false);
      }
    });
    return Arrays.asList (scc);
  }

  private static <PropT> void visit (Node<PropT> p, SCCState<PropT> s) {
    s.L.add(0, p);
    p.setIntAttribute("_dfsnum", s.N);
    p.setIntAttribute("_low", s.N);
    s.N++;

    for (Edge<PropT> e: p.getOutgoingEdges()) {
      Node<PropT> q = e.getNext();

      if (!q.getBooleanAttribute("_reached")) {
        q.setBooleanAttribute("_reached", true);
        visit(q, s);
        p.setIntAttribute("_low", 
                          Math.min(p.getIntAttribute("_low"), 
                                   q.getIntAttribute("_low")));
      } else if (q.getIntAttribute("_dfsnum") < p.getIntAttribute("_dfsnum")) {
        if (s.L.contains(q)) {
          p.setIntAttribute("_low", 
                            Math.min(p.getIntAttribute("_low"), 
                                     q.getIntAttribute("_dfsnum")));
        }
      }
    }

    if (p.getIntAttribute("_low") == p.getIntAttribute("_dfsnum")) {
      Node<PropT> v;

      do {
        v = s.L.remove(0);
        v.setIntAttribute("_scc", s.SCC);
      } while (v != p);

      s.SCC++;
    }
  }

  /**
   * DOCUMENT ME!
   */
  private static class SCCState<PropT> {
    public int  N = 0;
    public int  SCC = 0;
    public List<Node<PropT>> L = new LinkedList<Node<PropT>>();
  }
}
