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

import java.util.*;


/**
 * DOCUMENT ME!
 */
public class Node<PropT> implements Comparable<Node<PropT>> { // removed (non-conforming) -pcd
  private int            accepting_conds = 0;
  // This is only ever checked if nodeId == 0 anyway:
  private boolean init_collapsed = false;
  private int            nodeId;
  private TreeSet<Node<PropT>>    incoming;
  private TreeSet<Formula<PropT>> toBeDone;
  private TreeSet<Formula<PropT>> old;
  private TreeSet<Formula<PropT>> next;
  private BitSet         accepting;
  private BitSet         right_of_untils;
  private Node<PropT>    OtherTransitionSource;
  private int equivalenceId;
  private Pool pool;

  Node (Pool pool, int accepting_conds) {
    this.pool = pool;
    this.accepting_conds = accepting_conds;
    nodeId = pool.requestId ();
    incoming = new TreeSet<Node<PropT>>();
    toBeDone = new TreeSet<Formula<PropT>>();
    old = new TreeSet<Formula<PropT>>();
    next = new TreeSet<Formula<PropT>>();
    OtherTransitionSource = null;
    accepting = new BitSet(accepting_conds);
    right_of_untils = new BitSet(accepting_conds);
  }

  Node (Pool pool, int accepting_conds, TreeSet<Node<PropT>> in,
        TreeSet<Formula<PropT>> newForm, TreeSet<Formula<PropT>> done,
        TreeSet<Formula<PropT>> nx, BitSet acc, BitSet rous) {
    this.pool = pool;
    this.accepting_conds = accepting_conds;
    nodeId = pool.requestId ();
    incoming = new TreeSet<Node<PropT>>(in);
    toBeDone = new TreeSet<Formula<PropT>>(newForm);
    old = new TreeSet<Formula<PropT>>(done);
    next = new TreeSet<Formula<PropT>>(nx);
    OtherTransitionSource = null;
    accepting = new BitSet(accepting_conds);
    accepting.or(acc);
    right_of_untils = new BitSet(accepting_conds);
    right_of_untils.or(rous);
  }

  public int getAcceptingConds () {
    return accepting_conds;
  }

  public static <PropT> Node<PropT> createInitial (Formula<PropT> form, Pool pool) {
    int accepting_conds = form.init_acc_sets(); // first mark right forms of untils;

    //    System.out.println("Accepting conditions: " + accepting_conds);
    Node<PropT> init = new Node<PropT> (pool, accepting_conds);
    assert init.nodeId == 0;

    if (form.getContent() != Formula.Content.TRUE) {
      init.decompose_ands_for_next(form);
    }

    return init;
  }

  public TreeSet<Formula<PropT>> getField_next () {
    return next;
  }

  public TreeSet<Formula<PropT>> getField_old () {
    return old;
  }

  public int getId () {
    return nodeId;
  }

  public boolean isInitial () {
    return nodeId == 0;
  }

  public int getNodeId () {
    return nodeId;
  }

  public void RTstructure (State<PropT>[] RTautomaton) {
    boolean safety = false;

    if (RTautomaton[nodeId] == null) {
      RTautomaton[nodeId] = new State<PropT>(accepting, equivalenceId);
    } else {
      RTautomaton[nodeId].updateAcc(accepting, equivalenceId);
    }

    if (is_safety_acc_node()) {
      RTautomaton[nodeId].updateSafetyAcc(true);
      safety = true;
    }

    Node<PropT> Alternative = this;

    while (Alternative != null) {
      for (Node<PropT> nextNode: Alternative.incoming) {
        int stateId = nextNode.getId();

        if (RTautomaton[stateId] == null) {
          RTautomaton[stateId] = new State<PropT>();
        }

        RTautomaton[stateId].add(
              new Transition<PropT>(Alternative.old, equivalenceId,
                                    accepting, safety, accepting_conds));
      }

      Alternative = Alternative.OtherTransitionSource;
    }
  }

  public int compareTo (Node<PropT> f) {
    if (this == f) {
      return 0;
    } else {
      return 1;
    }
  }

  public boolean compare_accepting (Node<PropT> nd) {
    //if (nodeId == 0) 
    //	System.out.println("Has it been collapsed yet? : " + init_collapsed);
    if ((nodeId == 0) && !init_collapsed) {
      // System.out.println("Potentially collapse " + nodeId + " with " + nd.nodeId);
      return true;
    }

    return (accepting.equals(nd.accepting)); // compare their BitSets
  }

  /*
     public void print() 
     {
       System.out.println("\n\nPrinting node " + nodeId);
       Iterator iterNext = next.iterator();
       Formula nextForm = null;
     
         // all formulas present must be of type V or W, otherwise false
         while (iterNext.hasNext()) 
         {
           nextForm = (Formula) iterNext.next();
           System.out.println("Formula: " + nextForm.toString());
         }
       }
   */
  public void debug () {
    System.out.println("debugging now");
    for (Formula<PropT> nextForm: old)
      System.out.println("Content is " + nextForm.getContent());
  }

  public void decompose_ands_for_next (Formula<PropT> form) {
    if (form.getContent() == Formula.Content.AND) {
      decompose_ands_for_next(form.getSub1());
      decompose_ands_for_next(form.getSub2());
    } else if (is_redundant(next, null, form) == false) {
      next.add(form);
    }
  }

  public Automaton<PropT> expand (Automaton<PropT> states) {
    //		System.out.println("expand entered"); // debugging
    Node<PropT> tempNode;

    if (toBeDone.isEmpty()) {
      if (nodeId != 0) {
        update_accepting();
      }


      // System.out.println("New is empty!");
      tempNode = states.alreadyThere(this);

      if (tempNode != null) {
        // System.out.println("Node " + nodeId + " collapsed with " + tempNode.nodeId);
        tempNode.modify(this);

        return states;
      } else {
        Node<PropT> NewN = new Node<PropT> (pool, accepting_conds);
        NewN.incoming.add(this);
        NewN.toBeDone.addAll(next);

        states.add(this);

        return (NewN.expand(states));
      }
    } else // toBeDone is not empty
    {
      Formula<PropT> temp_form;
      Formula<PropT> ita = toBeDone.first();
      toBeDone.remove(ita);

      //System.out.println("\n\nExpanding " + ita + " for node " + nodeId);
      if (testForContradictions(ita)) {
        //System.out.println("Finished expand - contradiction");
        return states;
      }

      // no contradiction
      // look in tech report why we do this even when ita is redundant
      if (ita.is_right_of_until(accepting_conds)) {
        right_of_untils.or(ita.get_rightOfWhichUntils());
      }

      TreeSet<Formula<PropT>> set_checked_against = new TreeSet<Formula<PropT>>();
      set_checked_against.addAll(old);
      set_checked_against.addAll(toBeDone);

      if (is_redundant(set_checked_against, next, ita)) {
        return expand(states);
      }

      // not redundant either
      // look in tech report why this only when not redundant
      if (ita.getContent() == Formula.Content.UNTIL) { // this is an until formula 
        accepting.set(ita.get_untils_index());

        //      	System.out.println("Just set an eventuality requirement");
      }

      if (!ita.isLiteral()) {
        switch (ita.getContent()) {
        case UNTIL:
        case WEAK_UNTIL:
        case RELEASE:
        case OR:
          Node<PropT> node2 = split(ita);
          return node2.expand(this.expand(states));
        case NEXT:
          decompose_ands_for_next(ita.getSub1());
          return expand(states);
        case AND:
          temp_form = ita.getSub1();

          if (!old.contains(temp_form)) {
            toBeDone.add(temp_form);
          }

          temp_form = ita.getSub2();

          if (!old.contains(temp_form)) {
            toBeDone.add(temp_form);
          }

          return expand(states);
        default:
          System.out.println("default case of switch entered");

          return null;
        }
      } else // ita represents a literal
      {
        //	System.out.println("Now working on literal " + ita.getContent());
        // must do a test for contradictions first
        if (ita.getContent() != Formula.Content.TRUE) {
          old.add(ita);
        }

        //	System.out.println("added to " + nodeId + " formula " + ita);
        return (expand(states));
      }
    }
  }

  public int get_equivalenceId () {
    return equivalenceId;
  }

  public void set_equivalenceId (int value) {
    equivalenceId = value;
  }

  public void update_accepting () {
    accepting.andNot(right_of_untils);

    // just do now the bitwise or so that accepting gets updated
  }

  private static <PropT> boolean is_redundant (TreeSet<Formula<PropT>> main_set, 
                                               TreeSet<Formula<PropT>> next_set, 
                                               Formula<PropT> ita) {
    if ((ita.is_special_case_of_V(main_set)) || // my addition - correct??? 
        ((ita.is_synt_implied(main_set, next_set)) && 
              (!(ita.getContent() == Formula.Content.UNTIL) || 
                (ita.getSub2().is_synt_implied(main_set, next_set))))) {
      //System.out.println("Looks like formula was redundant");
      return true;
    } else {
      return false;
    }
  }

  private boolean is_safety_acc_node () {
    if (next.isEmpty()) {
      return true;
    }

    Iterator<Formula<PropT>> iterNext = next.iterator();
    Formula<PropT> nextForm = null;

    // all formulas present must be of type V or W, otherwise false
    while (iterNext.hasNext()) {
      nextForm = iterNext.next();

      if ((nextForm.getContent() != Formula.Content.RELEASE) && 
          (nextForm.getContent() != Formula.Content.WEAK_UNTIL)) {
        return false;
      }
    }

    return true;
  }

  private void modify (Node<PropT> current) {
    boolean match = false;
    Node<PropT> Tail = this;
    Node<PropT> Alternative = this;

    if ((this.nodeId == 0) && !init_collapsed) {
      accepting = current.accepting;
      init_collapsed = true;
    }

    while (Alternative != null) {
      if (Alternative.old.equals(current.old)) {
        Alternative.incoming.addAll(current.incoming);
        match = true;
      }

      Tail = Alternative;
      Alternative = Alternative.OtherTransitionSource;
    }

    if (!match) {
      Tail.OtherTransitionSource = current;
    }
  }

  private Node<PropT> split (Formula<PropT> form) {
    //System.out.println("Split is entered");
    Formula<PropT> temp_form;

    // first create Node 2
    Node<PropT> Node2 = new Node<PropT> (pool, accepting_conds,
        this.incoming, this.toBeDone, this.old, this.next, 
        this.accepting, this.right_of_untils);

    temp_form = form.getSub2();

    if (!old.contains(temp_form)) //New2(n) not in old

    {
      Node2.toBeDone.add(temp_form);
    }

    if (form.getContent() == Formula.Content.RELEASE) // both subformulas are added to New2
    {
      temp_form = form.getSub1();

      if (!old.contains(temp_form)) // subformula not in old

      {
        Node2.toBeDone.add(temp_form);
      }
    }


    // then substitute current Node with Node 1
    temp_form = form.getSub1();

    if (!old.contains(temp_form)) //New1(n) not in old

    {
      toBeDone.add(temp_form);
    }

    temp_form = form.getNext();

    if (temp_form != null) {
      decompose_ands_for_next(temp_form);
    }

    /* following lines are probably unecessary because we never split literals!*/
    if (form.isLiteral())/* because we only store literals... */
    {
      old.add(form);
      System.out.println("added " + form); // never supposed to see that
      Node2.old.add(form);
    }

    //System.out.println("Node split into itself and node : " + Node2.nodeId);
    //print();
    //Node2.print();
    return Node2;
  }

  private boolean testForContradictions (Formula<PropT> ita) {
    Formula<PropT> Not_ita = ita.negate();

    if (Not_ita.is_synt_implied(old, next)) {
      return true;
    } else {
      return false;
    }
  }
}
