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

import gov.nasa.ltl.graphio.Reader;

import java.io.*;

import java.util.BitSet;
import java.util.List;


/**
 * DOCUMENT ME!
 */
public class SCCReduction {
  public static void main (String[] args) {
    if (args.length > 1) {
      System.out.println("usage:");
      System.out.println("\tjava gov.nasa.ltl.graph.SCCReduction [<filename>]");

      return;
    }

    Graph<String> g = null;

    try {
      if (args.length == 0) {
        g = Reader.read();
      } else {
        g = Reader.read(args[0]);
      }
    } catch (IOException e) {
      System.out.println("Can't load the graph.");

      return;
    }

    g = reduce(g);

    g.save();
  }

  public static <PropT> Graph<PropT> reduce (Graph<PropT> g) {
    boolean changed;
    /* not needed? - pcd
    String  type = g.getStringAttribute("type");
    String  ac = g.getStringAttribute("ac");
    boolean acNodes = ac.equals("nodes");*/

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

  private static <PropT> boolean isAccepting (List<Node<PropT>> scc, Graph<PropT> g) {
    String type = g.getStringAttribute("type");
    String ac = g.getStringAttribute("ac");

    if (type.equals("ba")) {
      if (ac.equals("nodes")) {
        for (Node<PropT> n: scc) {
          if (n.getBooleanAttribute("accepting")) {
            return true;
          }
        }

        return false;
      } else if (ac.equals("edges")) {
        for (Node<PropT> n: scc) {
          for (Edge<PropT> e: n.getOutgoingEdges ()) {
            if (e.getBooleanAttribute("accepting")) {
              return true;
            }
          }
        }

        return false;
      } else {
        throw new RuntimeException("invalid accepting type: " + ac);
      }
    } else if (type.equals("gba")) {
      int    nsets = g.getIntAttribute("nsets");
      BitSet found = new BitSet(nsets);
      int    nsccs = 0;

      if (ac.equals("nodes")) {
        for (Node<PropT> n: scc) {
          for (int j = 0; j < nsets; j++) {
            if (n.getBooleanAttribute("acc" + j)) {
              if (!found.get(j)) {
                found.set(j);
                nsccs++;
              }
            }
          }
        }
      } else if (ac.equals("edges")) {
        for (Node<PropT> n: scc) {
          for (Edge<PropT> e: n.getOutgoingEdges ()) {
            for (int k = 0; k < nsets; k++) {
              if (e.getBooleanAttribute("acc" + k)) {
                if (!found.get(k)) {
                  found.set(k);
                  nsccs++;
                }
              }
            }
          }
        }
      } else {
        throw new RuntimeException("invalid accepting type: " + ac);
      }

      return nsccs == nsets;
    } else {
      throw new RuntimeException("invalid graph type: " + type);
    }
  }

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

  private static <PropT> boolean anyAcceptingState (List<Node<PropT>> scc, Graph<PropT> g) {
    String type = g.getStringAttribute("type");
    String ac = g.getStringAttribute("ac");

    if (type.equals("ba")) {
      if (ac.equals("nodes")) {
        for (Node<PropT> n: scc) {
          if (n.getBooleanAttribute("accepting")) {
            return true;
          }
        }
      } else if (ac.equals("edges")) {
        for (Node<PropT> n: scc) {
          for (Edge<PropT> e: n.getOutgoingEdges ()) {
            if (e.getBooleanAttribute("accepting")) {
              return true;
            }
          }
        }
      } else {
        throw new RuntimeException("invalid accepting type: " + ac);
      }
    } else if (type.equals("gba")) {
      int nsets = g.getIntAttribute("nsets");

      if (ac.equals("nodes")) {
        for (Node<PropT> n: scc) {
          for (int j = 0; j < nsets; j++) {
            if (n.getBooleanAttribute("acc" + j)) {
              return true;
            }
          }
        }
      } else if (ac.equals("edges")) {
        for (Node<PropT> n: scc) {
          for (Edge<PropT> e: n.getOutgoingEdges ()) {
            for (int k = 0; k < nsets; k++) {
              if (e.getBooleanAttribute("acc" + k)) { // TODO: was Iterator j; verify…
                return true;
              }
            }
          }
        }
      } else {
        throw new RuntimeException("invalid accepting type: " + ac);
      }
    } else {
      throw new RuntimeException("invalid graph type: " + type);
    }

    return false;
  }

  private static <PropT> void clearAccepting (List<Node<PropT>> scc, Graph<PropT> g) {
    String type = g.getStringAttribute("type");
    String ac = g.getStringAttribute("ac");

    if (type.equals("ba")) {
      if (ac.equals("nodes")) {
        for (Node<PropT> n: scc) {
          n.setBooleanAttribute("accepting", false);
        }
      } else if (ac.equals("edges")) {
        for (Node<PropT> n: scc) {
          for (Edge<PropT> e: n.getOutgoingEdges ()) {
            e.setBooleanAttribute("accepting", false);
          }
        }
      } else {
        throw new RuntimeException("invalid accepting type: " + ac);
      }
    } else if (type.equals("gba")) {
      int nsets = g.getIntAttribute("nsets");

      if (ac.equals("nodes")) {
        for (Node<PropT> n: scc) {
          for (int j = 0; j < nsets; j++) {
            n.setBooleanAttribute("acc" + j, false);
          }
        }
      } else if (ac.equals("edges")) {
        for (Node<PropT> n: scc) {
          for (Edge<PropT> e: n.getOutgoingEdges ()) {
            for (int k = 0; k < nsets; k++) {
              e.setBooleanAttribute("acc" + k, false);
            }
          }
        }
      } else {
        throw new RuntimeException("invalid accepting type: " + ac);
      }
    } else {
      throw new RuntimeException("invalid graph type: " + type);
    }
  }

  private static <PropT> void clearExternalEdges (List<Node<PropT>> scc, Graph<PropT> g) {
    String type = g.getStringAttribute("type");
    String ac = g.getStringAttribute("ac");

    if (type.equals("ba")) {
      if (ac.equals("nodes")) {
      } else if (ac.equals("edges")) {
        for (Node<PropT> n: scc) {
          for (Edge<PropT> e: n.getOutgoingEdges ()) {
            if (!scc.contains(e.getNext())) {
              e.setBooleanAttribute("accepting", false);
            }
          }
        }
      } else {
        throw new RuntimeException("invalid accepting type: " + ac);
      }
    } else if (type.equals("gba")) {
      int nsets = g.getIntAttribute("nsets");

      if (ac.equals("nodes")) {
      } else if (ac.equals("edges")) {
        for (Node<PropT> n: scc) {
          for (Edge<PropT> e: n.getOutgoingEdges ()) {
            if (!scc.contains(e.getNext())) {
              for (int k = 0; k < nsets; k++) {
                e.setBooleanAttribute("acc" + k, false);
              }
            }
          }
        }
      } else {
        throw new RuntimeException("invalid accepting type: " + ac);
      }
    } else {
      throw new RuntimeException("invalid graph type: " + type);
    }
  }
}
