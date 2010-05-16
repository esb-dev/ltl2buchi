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

import java.util.LinkedList;

import gov.nasa.ltl.graph.*;


/**
 * DOCUMENT ME!
 */
class Automaton<PropT> {
  private LinkedList<Node<PropT>> nodeList = new LinkedList<Node<PropT>>();
  private Node<PropT>[]   equivalence_classes; // array of representatives of equivalent states
  private Pool pool;

  Automaton (Pool pool) {
    equivalence_classes = null;
    this.pool = pool;
  }

  @SuppressWarnings ("unchecked")
  public Graph<PropT> SMoutput (State<PropT>[] automaton) {
    int accepting_conds;
    Graph<PropT> g = new Graph<PropT>();
    g.setStringAttribute("type", "gba");
    g.setStringAttribute("ac", "edges");

    if (automaton == null) {
      return g;
    }

    int size = pool.getIdCount ();
    gov.nasa.ltl.graph.Node<PropT>[] nodes = new gov.nasa.ltl.graph.Node[size];

    for (int i = 0; i < size; i++) {
      if ((automaton[i] != null) && 
              (i == automaton[i].getRepresentativeId())) {
        nodes[i] = new gov.nasa.ltl.graph.Node<PropT> (g);
        nodes[i].setStringAttribute("label", 
                                    "S" + automaton[i].getRepresentativeId());
      }
    }

    for (int i = 0; i < size; i++) {
      if ((automaton[i] != null) && 
              (i == automaton[i].getRepresentativeId())) {
        automaton[i].SMoutput(nodes, nodes[i]);
      }
    }
    
    assert nodeList.size () > 0;
    accepting_conds = nodeList.get (0).getAcceptingConds ();
    if (accepting_conds == 0) {
      g.setIntAttribute("nsets", 1);
    } else {
      g.setIntAttribute("nsets", accepting_conds);
    }

    return g;
  }

  public void add (Node<PropT> nd) {
    nodeList.add (nd);
  }

  public Node<PropT> alreadyThere (Node<PropT> nd) {
    /* when running LTL2Buchi is already there if next fields and 
       accepting conditions are the same. For LTL2AUT, old fields
       also have to be the same
     */
    for (Node<PropT> currState: nodeList) {
      if (currState.getField_next().equals(nd.getField_next()) && 
              currState.compare_accepting(nd) && 
              (Translator.getAlgorithm() == Translator.Algorithm.LTL2BUCHI || 
                (currState.getField_old().equals(nd.getField_old())))) {
        //System.out.println("Match found");
        return currState;
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

    for (index = 0; index <= pool.lastId (); index++) {
      if (equivalence_classes[index] == null) {
        //	System.out.println("Null object");
        break;
      } else if ((Translator.getAlgorithm() == Translator.Algorithm.LTL2BUCHI) && 
                     (equivalence_classes[index].getField_next().equals(nd.getField_next()))) {
        //	System.out.println("Successful merge");
        return (equivalence_classes[index].getNodeId());
      }
    }

    assert index <= pool.lastId () :
      "ERROR - size of equivalence classes array was incorrect";

    equivalence_classes[index] = nd;

    return (equivalence_classes[index].getNodeId());
  }

  @SuppressWarnings ("unchecked")
  public State<PropT>[] structForRuntAnalysis () {
    // now also fixes equivalence classes
    pool.stop ();

    int automatonSize = pool.getIdCount ();
    State<PropT>[] RTstruct = new State[automatonSize];
    equivalence_classes = new Node[automatonSize];

    for (Node<PropT> current: nodeList) {
      current.set_equivalenceId(index_equivalence(current));
      current.RTstructure(RTstruct);
    }
    return RTstruct;
  }
}
