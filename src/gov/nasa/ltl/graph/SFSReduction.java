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
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;


/**
 * DOCUMENT ME!
 */
public class SFSReduction {
  @SuppressWarnings ("unchecked")
  public static <PropT> Graph<PropT> reduce (Graph<PropT> g) {
    // debugged by Dimitra 3/4/02 - added |PO| information so that main while
    // loop works correctly - removed break statement based on color only
    int        currNumColors;
    int        prevNumColors = 1;
    int        currNumPO = 3;
    int        prevNumPO = 1;
    TreeSet<ColorPair<PropT>> newColorSet = null;
    LinkedList<Pair<ColorPair<PropT>>> newColorList = null;
    boolean    accepting = false;
    boolean    nonaccepting = false;

    // Initialization
    List<Node<PropT>> nodes = g.getNodes();

    for (Node<PropT> currNode: nodes) {
      currNode.setIntAttribute("_prevColor", 1);

      if (isAccepting(currNode)) {
        currNode.setIntAttribute("_currColor", 1);
        accepting = true;
      } else {
        currNode.setIntAttribute("_currColor", 2);
        nonaccepting = true;
      }
    }

    if (accepting && nonaccepting) {
      currNumColors = 2;
    } else {
      currNumColors = 1;
    }

    // po(i, j)
    boolean[][] currPO = new boolean[2][2];
    boolean[][] prevPO;

    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        if (i >= j) {
          currPO[i][j] = true;
        } else {
          currPO[i][j] = false;
        }
      }
    }

    while ((currNumColors != prevNumColors) || (currNumPO != prevNumPO)) {
      // Incrementing i, equiv. current values become previous ones
      for (Node<PropT> currNode: nodes) {
        currNode.setIntAttribute("_prevColor", 
                                 currNode.getIntAttribute("_currColor"));
      }

      prevPO = currPO;
      prevNumColors = currNumColors;


      // Getting the new color pairs
      newColorList = new LinkedList<Pair<ColorPair<PropT>>>(); // keeps association of node with new color
      newColorSet = new TreeSet<ColorPair<PropT>>(); // keeps set of new colors

      for (Node<PropT> currNode: nodes) {
        ColorPair<PropT> currPair = new ColorPair<PropT>(currNode.getIntAttribute(
                                                 "_prevColor"), 
                                           getPrevN(currNode, prevPO));


        /*    System.out.println("Transition set from node: " + currNode.getId()
           + " is: " + currPair.getIMaxSet());
         */
        newColorList.add(new Pair<ColorPair<PropT>>(currNode.getId(), currPair));
        newColorSet.add(currPair);
      }

      currNumColors = newColorSet.size();

//      	  System.out.println("The number of colors is: " + currNumColors + "\n");
      // Dimitra comments
      // Convert the set into a linked list so that rank of object is known
      // originally used set to avoid duplicates 
      // rank will just be the position of the object in the list
      LinkedList<ColorPair<PropT>> ordered =
        new LinkedList<ColorPair<PropT>> (newColorSet);
      // Renaming color set
      for (Pair<ColorPair<PropT>> cPair: newColorList) {
        ColorPair<PropT> currPair = cPair.getElement();
        g.getNode(cPair.getValue())
         .setIntAttribute("_currColor", ordered.indexOf(currPair) + 1);
      }


      // Update partial order
      prevNumPO = currNumPO;
      currNumPO = 0;
      currPO = new boolean[currNumColors][currNumColors];

      for (Pair<ColorPair<PropT>> currPairOneP: newColorList) {
        ColorPair<PropT> currPairOne = currPairOneP.getElement ();
        for (Pair<ColorPair<PropT>> currPairTwoP: newColorList) {
          ColorPair<PropT> currPairTwo = currPairTwoP.getElement ();
          boolean   po = prevPO[currPairTwo.getColor() - 1][currPairOne.getColor() - 1];
          boolean   dominate = iDominateSet(currPairOne.getIMaxSet(), 
                                            currPairTwo.getIMaxSet(), prevPO);

          if (po && dominate) {
            currPO[ordered.indexOf(currPairTwo)][ordered.indexOf(currPairOne)] = true;
            currNumPO++;
          } else {
            currPO[ordered.indexOf(currPairTwo)][ordered.indexOf(currPairOne)] = false;
          }
        }
      }
    }

    // Create new graph
    Graph<PropT> result;

    if (newColorList == null) {
      result = g;
    } else {
      result = new Graph<PropT>();

      Node<PropT>[] newNodes = (Node<PropT>[])new Node[currNumColors];

      for (int i = 0; i < currNumColors; i++) {
        Node<PropT> n = new Node<PropT>(result);
        newNodes[i] = n;
      }

      for (Pair<ColorPair<PropT>> nodePair: newColorList) {
        int           origNodeId = nodePair.getValue();
        ColorPair<PropT> colPair = nodePair.getElement();

        if (newColorSet != null && newColorSet.contains(colPair)) {
          // for all transitions based on colors, newColorSet makes sure that
          // no duplicates exist, neither transitions that dominate each other 
          // that is why we only add transitions that belong to it;
          // I guess we could also just use all transitions in newColorSet to
          // create the new transition relation 
          newColorSet.remove(colPair);

          TreeSet<ITypeNeighbor<PropT>> pairSet = colPair.getIMaxSet();
          int color = colPair.getColor();
          Node<PropT> currNode = newNodes[color - 1];

          for (ITypeNeighbor<PropT> neigh: pairSet) {
            int           neighPos = neigh.getColor() - 1;
            // for side effect in constructor:
            new Edge<PropT>(currNode, newNodes[neighPos], neigh.getTransition());
          }

          // starting node
          if (g.getInit().getId() == origNodeId) {
            result.setInit(currNode);
          }

          // accepting node
          if (isAccepting(g.getNode(origNodeId))) {
            currNode.setBooleanAttribute("accepting", true);
          }
        } else {
        } // ignore such transitions
      }
    }

    return reachabilityGraph(result);

    //return result;
  }

  private static <PropT> boolean isAccepting (Node<PropT> nodeIn) {
    return (nodeIn.getBooleanAttribute("accepting"));
  }

  private static <PropT> TreeSet<ITypeNeighbor<PropT>> getPrevN (
      Node<PropT> currNode, boolean[][] prevPO) {
    List<Edge<PropT>>     edges = currNode.getOutgoingEdges();
    LinkedList<ITypeNeighbor<PropT>> neighbors = new LinkedList<ITypeNeighbor<PropT>>();
    ITypeNeighbor<PropT> iNeigh;
    TreeSet<ITypeNeighbor<PropT>> prevN = new TreeSet<ITypeNeighbor<PropT>>();

    for (Edge<PropT> currEdge: edges) {
      iNeigh = new ITypeNeighbor<PropT>(currEdge.getNext()
                                         .getIntAttribute("_prevColor"), 
                                        currEdge.getGuard());
      neighbors.add(iNeigh);
    }

    // No neighbors present
    if (neighbors.size() == 0) {
      return prevN;
    }

    // Get the first of the list. Remove it from the list. Compare
    // this element with the rest of the list. If element subsumes
    // something in remainder of list remove that thing from list. If
    // element is subsumed, throw element away, else put element in
    // set
    boolean useless;

    do {
      useless = false;
      iNeigh = neighbors.removeFirst();

      for (Iterator<ITypeNeighbor<PropT>> i = neighbors.iterator(); i.hasNext();) {
        ITypeNeighbor<PropT> nNeigh = i.next();
        ITypeNeighbor<PropT> dominating = iDominates(iNeigh, nNeigh, prevPO);

        if (dominating == iNeigh) {
          i.remove();
        }

        if (dominating == nNeigh) {
          useless = true;

          break;
        }
      }

      if (!useless) {
        prevN.add(iNeigh);
      }
    } while (neighbors.size() > 0);

    return prevN;
  }

  private static <PropT> boolean iDominateSet (
      TreeSet<ITypeNeighbor<PropT>> setOne,
      TreeSet<ITypeNeighbor<PropT>> setTwo, 
      boolean[][] prevPO) {
    TreeSet<ITypeNeighbor<PropT>> working = new TreeSet<ITypeNeighbor<PropT>>(setTwo);

    for (ITypeNeighbor<PropT> neighTwo: setTwo) {
      for (ITypeNeighbor<PropT> neighOne: setOne) {
        ITypeNeighbor<PropT> dominating = iDominates(neighOne, neighTwo, prevPO);

        if (dominating == neighOne) {
          working.remove (neighTwo);
          break;
        }
      }
    }
    return working.isEmpty ();
  }
  
  /** Returns the neighbor that dominates. If none dominates the
   * other, then returns null
   */
  private static <PropT> ITypeNeighbor<PropT> iDominates (
      ITypeNeighbor<PropT> iNeigh, 
      ITypeNeighbor<PropT> nNeigh, 
      boolean[][] prevPO) {
    Guard<PropT> iTerm = iNeigh.getTransition();
    Guard<PropT> nTerm = nNeigh.getTransition();
    int    iColor = iNeigh.getColor();
    int    nColor = nNeigh.getColor();
    
    if ((iTerm.subtermOf (nTerm) || iTerm.isTrue ())
        && prevPO[nColor-1][iColor-1])
      return iNeigh;
    if ((nTerm.subtermOf (iTerm) || nTerm.isTrue ())
        && prevPO[iColor-1][nColor-1])
      return nNeigh;
    return null;
  }

  private static <PropT> Graph<PropT> reachabilityGraph (Graph<PropT> g) {
    Vector<Node<PropT>> work = new Vector<Node<PropT>>();
    Vector<Node<PropT>> reachable = new Vector<Node<PropT>>();
    work.add(g.getInit());

    while (!work.isEmpty()) {
      Node<PropT> currNode = work.firstElement();
      reachable.add(currNode);

      if (currNode != null) {
        for (Edge<PropT> currEdge: currNode.getOutgoingEdges ()) {
          Node<PropT> nextNode = currEdge.getNext();

          if (!(work.contains(nextNode) || reachable.contains(nextNode))) {
            work.add(nextNode);
          }
        }
      }

      Node<PropT> removed = work.remove (0);
      assert removed == currNode : "ERROR";
    }

    if (g.getNodes() != null) {
      for (Node<PropT> n: g.getNodes ()) {
        if (!reachable.contains(n)) {
          g.removeNode(n);
        }
      }
    }

    return g;
  }
}
