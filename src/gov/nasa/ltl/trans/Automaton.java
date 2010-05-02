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
package gov.nasa.ltl.trans;
//Written by Dimitra Giannakopoulou, 19 Jan 2001

import gov.nasa.ltl.graph.*;


/**
 * DOCUMENT ME!
 */
class LinkNode<PropT> {
  private Node<PropT>     node;
  private LinkNode<PropT> next;

  public LinkNode (Node<PropT> nd, LinkNode<PropT> nxt) {
    node = nd;
    next = nxt;
  }

  public LinkNode<PropT> getNext () {
    return next;
  }

  public Node<PropT> getNode () {
    return node;
  }

  public void LinkWith (LinkNode<PropT> lk) {
    next = lk;
  }
}

/**
 * DOCUMENT ME!
 */
class Automaton<PropT> {
  private LinkNode<PropT> head;
  private LinkNode<PropT> tail;
  private Node<PropT>[]   equivalence_classes; // array of representatives of equivalent states

  public Automaton () {
    head = tail = null;
    equivalence_classes = null;
  }

  public static <PropT> void FSPoutput (State<PropT>[] automaton) {
    boolean comma = false;

    if (automaton == null) {
      System.out.println("\n\nRES = STOP.");

      return;
    } else {
      System.out.println("\n\nRES = S0,");
    }

    int size = Pool.assign();

    for (int i = 0; i < size; i++) {
      if ((automaton[i] != null) && 
              (i == automaton[i].get_representativeId())) // a representative so print
      {
        if (comma) {
          System.out.println("),");
        }

        comma = true;
        System.out.print("S" + automaton[i].get_representativeId());
        System.out.print("=");
        automaton[i].FSPoutput();
      }
    }

    System.out.println(").\n");
  }

  public static <PropT> Graph SMoutput (State<PropT>[] automaton) {
    Graph g = new Graph();
    g.setStringAttribute("type", "gba");
    g.setStringAttribute("ac", "edges");

    if (automaton == null) {
      return g;
    }

    int                            size = Pool.assign();
    gov.nasa.ltl.graph.Node[] nodes = new gov.nasa.ltl.graph.Node[size];

    for (int i = 0; i < size; i++) {
      if ((automaton[i] != null) && 
              (i == automaton[i].get_representativeId())) {
        nodes[i] = new gov.nasa.ltl.graph.Node(g);
        nodes[i].setStringAttribute("label", 
                                    "S" + 
                                    automaton[i].get_representativeId());
      }
    }

    for (int i = 0; i < size; i++) {
      if ((automaton[i] != null) && 
              (i == automaton[i].get_representativeId())) {
        automaton[i].SMoutput(nodes, nodes[i]);
      }
    }

    if (Node.accepting_conds == 0) {
      g.setIntAttribute("nsets", 1);
    } else {
      g.setIntAttribute("nsets", Node.accepting_conds);
    }

    return g;
  }

  public void add (Node<PropT> nd) {
    LinkNode<PropT> newNode = new LinkNode<PropT>(nd, null);

    if (head == null) // set is currently empty
    {
      head = tail = newNode;
    } else // put element at end of list
    {
      tail.LinkWith(newNode);
      tail = newNode;
    }
  }

  public Node<PropT> alreadyThere (Node<PropT> nd) {
    /* when running LTL2Buchi is already there if next fields and 
       accepting conditions are the same. For LTL2AUT, old fields
       also have to be the same
     */
    LinkNode<PropT> nextNd = head;

    while (nextNd != null) {
      Node<PropT> currState = nextNd.getNode();

      if (currState.getField_next().equals(nd.getField_next()) && 
              currState.compare_accepting(nd) && 
              (Translator.get_algorithm() == Translator.LTL2BUCHI || 
                (currState.getField_old().equals(nd.getField_old())))) {
        //System.out.println("Match found");
        return currState;
      } else {
        nextNd = nextNd.getNext();
      }
    }

    //System.out.println("No match found for node " + nd.getNodeId());
    return null;
  }

  /*  public int get_representative_id(int automaton_index, State[] automaton)
     {
     return equivalence_classes[automaton[automaton_index].get_equivalence_class()].getNodeId();
     }
   */
  public int index_equivalence (Node<PropT> nd) {
    // check if next field of node is already represented
    int index;

    for (index = 0; index < Pool.assign(); index++) {
      if (equivalence_classes[index] == null) {
        //	System.out.println("Null object");
        break;
      } else if ((Translator.get_algorithm() == Translator.LTL2BUCHI) && 
                     (equivalence_classes[index].getField_next().equals(nd.getField_next()))) {
        //	System.out.println("Successful merge");
        return (equivalence_classes[index].getNodeId());
      }
    }

    if (index == Pool.assign()) {
      System.out.println(
            "ERROR - size of equivalence classes array was incorrect");
    }

    equivalence_classes[index] = nd;

    return (equivalence_classes[index].getNodeId());
  }

  @SuppressWarnings ("unchecked")
  public State<PropT>[] structForRuntAnalysis () {
    // now also fixes equivalence classes
    Pool.stop();

    int     automatonSize = Pool.assign();
    // Java cannot instantiate arrays of generic classes.
    State<PropT>[] RTstruct = (State<PropT>[])new State[automatonSize];
    equivalence_classes = (Node<PropT>[])new Node[automatonSize];

    if (head == null) {
      return RTstruct;
    }

    LinkNode<PropT> nextNd = head;
    Node<PropT>     current;

    while (nextNd != null) {
      current = nextNd.getNode();
      current.set_equivalenceId(index_equivalence(current));
      nextNd.getNode().RTstructure(RTstruct);
      nextNd = nextNd.getNext();
    }

    return RTstruct;
  }
}
