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
//Written by Dimitra Giannakopoulou, 29 Jan 2001

import java.util.*;


/**
 * DOCUMENT ME.
 */
public class State<PropT> {//implements Comparable<State> { // removed (non-conforming) -pcd
  private int        representativeId = -1;
  private LinkedList<Transition<PropT>> transitions;
  private BitSet     accepting;
  private boolean    safety_acceptance;

  public State (BitSet acc) {
    transitions = new LinkedList<Transition<PropT>>();
    accepting = acc;
    safety_acceptance = false;
  }

  public State (BitSet acc, int equivId) {
    transitions = new LinkedList<Transition<PropT>>();
    accepting = acc;
    safety_acceptance = false;
    representativeId = equivId;
  }

  public State () {
    transitions = new LinkedList<Transition<PropT>>();
    accepting = null;
    safety_acceptance = false;
  }

  public State (int equivId) {
    transitions = new LinkedList<Transition<PropT>>();
    accepting = null;
    safety_acceptance = false;
    representativeId = equivId;
  }

  /*  public boolean isAccepting()
     {
     return accepting;
     }
   */
  public void FSPoutput () {
    ListIterator<Transition<PropT>> iter = transitions.listIterator(0);
    Transition<PropT> nextTrans;
    boolean      first_trans = true;

    while (iter.hasNext()) {
      nextTrans = iter.next();

      if (first_trans) {
        System.out.print("(");
        first_trans = false;
      } else {
        System.out.print("|");
      }

      nextTrans.FSPoutput();
    }
  }

  public void SMoutput (gov.nasa.ltl.graph.Node<PropT>[] nodes, 
                        gov.nasa.ltl.graph.Node<PropT> node) {
    for (Transition<PropT> trans: transitions)
      trans.SMoutput(nodes, node);
  }

  public boolean accepts (int i) {
    return (!(accepting.get(i)));

    // because in my accepting array 0 corresponds to accepting
  }

  // <2do> pcm - CodeGuide Sapphire bug - need to explicitly qualify Transition
  // or it's mocking its use in Node
  public void add (Transition<PropT> trans) {
    transitions.add(trans);
  }

  public int compareTo (State<PropT> f) {
    if (this == f) {
      return 0;
    } else {
      return 1;
    }
  }

  public int get_representativeId () {
    return representativeId;
  }

  public boolean is_safe () {
    return safety_acceptance;
  }

  public void set_representativeId (int id) {
    representativeId = id;
  }

  public void step (Hashtable<PropT,Boolean> ProgramState,
                    TreeSet<State<PropT>> newStates, 
                    State<PropT>[] automaton) {
    for (Transition<PropT> nextTrans: transitions) {
      if (nextTrans.enabled(ProgramState)) {
        newStates.add(automaton[nextTrans.goesTo()]);
      }
    }
  }

  public void update_acc (BitSet acc) {
    accepting = acc;
  }

  public void update_acc (BitSet acc, int equivId) {
    accepting = acc;
    representativeId = equivId;
  }

  public void update_safety_acc (boolean val) {
    safety_acceptance = val;
  }

  /**
   * @return the transitions
   */
  public LinkedList<Transition<PropT>> getTransitions () {
    return transitions;
  }
}
