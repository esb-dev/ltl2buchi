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
public class State<PropT> {
  private int        representativeId = -1;
  private LinkedList<Transition<PropT>> transitions;
  private BitSet     accepting;
  private boolean    safety_acceptance;

  State (BitSet acc) {
    transitions = new LinkedList<Transition<PropT>>();
    accepting = acc;
    safety_acceptance = false;
  }

  State (BitSet acc, int equivId) {
    transitions = new LinkedList<Transition<PropT>>();
    accepting = acc;
    safety_acceptance = false;
    representativeId = equivId;
  }

  State () {
    transitions = new LinkedList<Transition<PropT>>();
    accepting = null;
    safety_acceptance = false;
  }

  State (int equivId) {
    transitions = new LinkedList<Transition<PropT>>();
    accepting = null;
    safety_acceptance = false;
    representativeId = equivId;
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

  void add (Transition<PropT> trans) {
    transitions.add(trans);
  }

  public int getRepresentativeId () {
    return representativeId;
  }

  public boolean isSafe () {
    return safety_acceptance;
  }

  void setRepresentativeId (int id) {
    representativeId = id;
  }

  public void step (Hashtable<PropT, Boolean> programState,
                    TreeSet<State<PropT>> newStates, 
                    State<PropT>[] automaton) {
    for (Transition<PropT> nextTrans: transitions) {
      if (nextTrans.enabled(programState)) {
        newStates.add(automaton[nextTrans.goesTo()]);
      }
    }
  }

  void updateAcc (BitSet acc) {
    accepting = acc;
  }

  void updateAcc (BitSet acc, int equivId) {
    accepting = acc;
    representativeId = equivId;
  }

  void updateSafetyAcc (boolean val) {
    safety_acceptance = val;
  }

  /**
   * @return the transitions
   */
  public LinkedList<Transition<PropT>> getTransitions () {
    return transitions;
  }
}
