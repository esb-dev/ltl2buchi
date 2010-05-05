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

import gov.nasa.ltl.graph.*;

import java.util.*;


/**
 * DOCUMENT ME!
 */
public class Transition<PropT> {
  private TreeSet<Formula<PropT>> propositions;
  private int       pointsTo;
  private BitSet    accepting;
  private boolean   safe_accepting;
  private Guard<PropT> guard;

  public Transition (TreeSet<Formula<PropT>> prop, int nd_id, BitSet acc, boolean safety) {
    propositions = prop;
    guard = new Guard<PropT> (prop);
    pointsTo = nd_id;
    accepting = new BitSet(Node.getAcceptingConds());
    accepting.or(acc);
    safe_accepting = safety;
  }

  public void FSPoutput () {
    if (propositions.isEmpty()) {
      System.out.print("TRUE{");
    } else {
      // first print the propositions involved
      Iterator<Formula<PropT>> it = propositions.iterator();
      Formula<PropT> nextForm = null;
      StringBuilder act = new StringBuilder();
      Formula.Content cont; // stores content of formula
      boolean       need_AND = false; // connect with AND multiple propositions

      while (it.hasNext()) {
        nextForm = it.next();
        cont = nextForm.getContent();

        if (need_AND) {
          act.append("_AND_");
        }

        need_AND = true;

        switch (cont) {
        case NOT:
          act.append('N');
          act.append(nextForm.getSub1().getName());

          break;

        case TRUE:
          act.append("TRUE");

          break;

        default:
          act.append(nextForm.getName());

          break;
        }
      }

      act.append('{');
      System.out.print(act);
    }

    if (Node.accepting_conds == 0) {
      if (safe_accepting == true) {
        System.out.print("0");
      }
    } else {
      for (int i = 0; i < Node.accepting_conds; i++) {
        if (!accepting.get(i)) {
          System.out.print(i);
        }
      }
    }


    // and then the rest - easy
    System.out.print("} -> S" + pointsTo + " ");
  }

  public void SMoutput (gov.nasa.ltl.graph.Node<PropT>[] nodes, 
                        gov.nasa.ltl.graph.Node<PropT> node) {
    String action = "-";
    Edge<PropT> e = new Edge<PropT>(node, nodes[pointsTo], guard, action);

    if (Node.accepting_conds == 0) {
      //  Dimitra - Jan 10 2003
      // Believe there is a bug with the way we decided whether node was safety accepting
      // with example !<>(Xa \/ <>c)
      //    System.out.println("Entered the safety part of accepting conditions");
      //      if (safe_accepting == true) {
      //        System.out.println("But did I actually set it correctly?");
      e.setBooleanAttribute("acc0", true);

      //      }
    } else {
      for (int i = 0; i < Node.accepting_conds; i++) {
        if (!accepting.get(i)) {
          e.setBooleanAttribute("acc" + i, true);

          //        System.out.println("Transition belongs to set " + i);
        }
      }
    }
  }

  public boolean enabled (Hashtable<PropT,Boolean> ProgramState) {
    for (Formula<PropT> literal: propositions) {
      PropT atom = literal.getName ();
      boolean negated = false;
      
      switch (literal.getContent ()) {
      case TRUE:
        break;
      case NOT:
        atom = literal.getSub1 ().getName ();
        negated = true;
        // fall through
      case PROPOSITION:
        if (!ProgramState.containsKey (atom))
          return false;
        if ((ProgramState.get (atom) && negated)
            || (!ProgramState.get (atom) && !negated))
          return false;
        break;
      default:
        assert false;
      }
    }
    return true;
  }


  public int goesTo () {
    return pointsTo;
  }

  /**
   * @return the safe_accepting
   */
  public boolean isSafeAccepting () {
    return safe_accepting;
  }

  /**
   * @return the guard
   */
  public Guard<PropT> getGuard () {
    return guard;
  }
  
  public boolean isAccepting (int i) {
    return accepting.get (i);
  }
}
