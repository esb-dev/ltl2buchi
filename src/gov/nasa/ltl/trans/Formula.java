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
//Parser by Flavio Lerda, 8 Feb 2001
//Parser extended by Flavio Lerda, 21 Mar 2001
//Modified to accept && and || by Roby Joehanes 15 Jul 2002

import java.util.*;

/**
 * DOCUMENT ME!
 */
public class Formula<PropT> implements Comparable<Formula<PropT>> {
  public static enum Content {
    PROPOSITION('p'),
    AND('A'),
    OR('O'),
    UNTIL('U'),
    RELEASE('V'),
    WEAK_UNTIL('W'),
    NOT('N'),
    NEXT('X'),
    TRUE('t'),
    FALSE('f');
    
    private final char c;
    
    private Content (char c) {
      this.c = c;
    }
    
    @Override
    public String toString () {
      return "" + c;
    }
  }
  private static int       nId = 0;
  private static HashSet<Formula<?>> cache = new HashSet<Formula<?>>();
  private Content          content;
  private Formula<PropT>   left;
  private Formula<PropT>   right;
  private int              id;
  private int              untils_index; // index to the untils vector
  private BitSet           rightOfWhichUntils; // for bug fix - formula can be right of >1 untils
  private PropT            name;
  private boolean          has_been_visited;

  private Formula (Content c, Formula<PropT> sx, Formula<PropT> dx, PropT n) {
    id = nId++;
    content = c;
    left = sx;
    right = dx;
    name = n;
    rightOfWhichUntils = null;
    untils_index = -1;
    has_been_visited = false;
  }
  
  Formula (Formula<PropT> f) {
    id = nId++;
    content = f.content;
    left = f.left;
    right = f.right;
    untils_index = f.untils_index;
    rightOfWhichUntils = f.rightOfWhichUntils != null ?
        (BitSet)f.rightOfWhichUntils.clone () : null;
    name = f.name;
    has_been_visited = f.has_been_visited;
  }

  public static void reset_static () {
    clearCache ();
  }

  public Content getContent () {
    return content;
  }

  public PropT getName () {
    return name;
  }

  public Formula<PropT> getNext () {
    switch (content) {
    case UNTIL:
    case WEAK_UNTIL:
    case RELEASE:
      return this;
    default:
      //    System.out.println(content + " Switch did not find a relevant case...");
      return null;
    }
  }

  public Formula<PropT> getSub1 () {
    if (content == Content.RELEASE) {
      return right;
    } else {
      return left;
    }
  }

  public Formula<PropT> getSub2 () {
    if (content == Content.RELEASE) {
      return left;
    } else {
      return right;
    }
  }

  public void addLeft (Formula<PropT> l) {
    left = l;
  }

  public void addRight (Formula<PropT> r) {
    right = r;
  }

  public int compareTo (Formula<PropT> f) {
    return (this.id - f.id);
  }

  public int countUntils (int acc_sets) {
    has_been_visited = true;

    if (getContent() == Content.UNTIL) {
      acc_sets++;
    }

    if ((left != null) && (!left.has_been_visited)) {
      acc_sets = left.countUntils(acc_sets);
    }

    if ((right != null) && (!right.has_been_visited)) {
      acc_sets = right.countUntils(acc_sets);
    }

    return acc_sets;
  }

  public BitSet get_rightOfWhichUntils () {
    return rightOfWhichUntils;
  }

  public int get_untils_index () {
    return untils_index;
  }

  public int initialize () {
    int acc_sets = countUntils(0);
    reset_visited();

//    if (false) System.out.println("Number of Us is: " + acc_sets);
    /*int test =*/ processRightUntils(0, acc_sets);
    reset_visited();

//    if (false) System.out.println("Number of Us is: " + test);
    return acc_sets;
  }

  public boolean is_literal () {
    switch (content) {
    case PROPOSITION:
    case TRUE:
    case FALSE:
      return true;
    case NOT:
      return getSub1 ().content == Content.PROPOSITION;
    default:
      return false;
    }
  }

  public boolean is_right_of_until (int size) {
    return (rightOfWhichUntils != null);
  }

  public boolean is_special_case_of_V (TreeSet<Formula<PropT>> check_against) {
    // necessary for Java’s type inference to do its work 
    Formula<PropT> tmp = False();
    Formula<PropT> form = Release(tmp, this);

    if (check_against.contains(form)) {
      return true;
    } else {
      return false;
    }
  }

  public boolean is_synt_implied (TreeSet<Formula<PropT>> old, TreeSet<Formula<PropT>> next) {
    if (this.getContent() == Content.TRUE) {
      return true;
    }

    if (old.contains(this)) {
      return true;
    }

    if (is_literal ())
      return false;
    
    Formula<PropT> form1 = this.getSub1();
    Formula<PropT> form2 = this.getSub2();
    Formula<PropT> form3 = this.getNext();

    boolean condition1 = true;
    boolean condition2 = true;
    boolean condition3 = true;

    if (form2 != null)
      condition2 = form2.is_synt_implied(old, next);
    if (form1 != null)
      condition1 = form1.is_synt_implied(old, next);
    if (form3 != null)
      if (next != null) {
        condition3 = next.contains(form3);
      } else {
        condition3 = false;
      }

    switch (getContent()) {
    case UNTIL:
    case WEAK_UNTIL:
    case OR:
      return (condition2 || (condition1 && condition3));
    case RELEASE:
      return ((condition1 && condition2) || (condition1 && condition3));
    case NEXT:
      if (form1 != null) {
        if (next != null) {
          return (next.contains(form1));
        } else {
          return false;
        }
      } else {
        return true;
      }
    case AND:
      return (condition2 && condition1);
    default:
      System.out.println("Default case of switch at Form.synt_implied");
      return false;
    }
  }

  public Formula<PropT> negate () {
    return Not(this);
  }

  public int processRightUntils (int current_index, int acc_sets) {
    has_been_visited = true;

    if (getContent() == Content.UNTIL) {
      this.untils_index = current_index;

      if (right.rightOfWhichUntils == null) {
        right.rightOfWhichUntils = new BitSet(acc_sets);
      }

      right.rightOfWhichUntils.set(current_index);
      current_index++;
    }

    if ((left != null) && (!left.has_been_visited)) {
      current_index = left.processRightUntils(current_index, acc_sets);
    }

    if ((right != null) && (!right.has_been_visited)) {
      current_index = right.processRightUntils(current_index, acc_sets);
    }

    return current_index;
  }

  public void reset_visited () {
    has_been_visited = false;

    if (left != null) {
      left.reset_visited();
    }

    if (right != null) {
      right.reset_visited();
    }
  }

  /**
   * Computes the length of the formula.
   * @return number of logical operators and atoms in the formula
   */
  public int size () {
    switch (content) {
    case AND:
    case OR:
    case UNTIL:
    case RELEASE:
    case WEAK_UNTIL:
      return left.size() + right.size() + 1;
    case NEXT:
    case NOT:
      return left.size() + 1;
    case PROPOSITION:
    case TRUE:
    case FALSE:
      return 1;
    }
    // can’t happen
    return 0;
  }

  public String toString (boolean exprId) {
    String idTag = exprId ? "[" + id + "]" : "";
    String conn = null;
    String r = null;

    switch (content) {
    case AND:
      conn = "/\\";
    case OR:
      if (conn == null) conn = "\\/";
    case UNTIL:
      if (conn == null) conn = "U";
    case RELEASE:
      if (conn == null) conn = "V";
    case WEAK_UNTIL:
      if (conn == null) conn = "W";
      r = "( " + left.toString (exprId) + " " + conn + " " +
        right.toString (exprId) + " )";
      break;
    //case 'M': return "( " + left.toString(true) + " M " + right.toString(true) + " )[" + id + "]";
    case NEXT:
      conn = "X";
    case NOT:
      if (conn == null) conn = "!";
      r = "( " + conn + " " + left.toString (exprId) + " )";
      break;
    case TRUE:
      conn = "true";
    case FALSE:
      if (conn == null) conn = "false";
    case PROPOSITION:
      if (conn == null) conn = name.toString ();
    default:
      if (conn == null) conn = content.toString ();
      r = "( " + conn + " )";
    }
    return r + idTag;
  }

  public String toString () {
    return toString (false);
  }

  public static <PropT> Formula<PropT> Always (Formula<PropT> f) {
    // necessary for Java’s type inference to do its work 
    Formula<PropT> tmp = False();
    return unique(new Formula<PropT>(Content.RELEASE, tmp, f, null));
  }

  public static <PropT> Formula<PropT> And (Formula<PropT> sx, Formula<PropT> dx) {
    if (sx.id < dx.id) {
      return unique(new Formula<PropT>(Content.AND, sx, dx, null));
    } else {
      return unique(new Formula<PropT>(Content.AND, dx, sx, null));
    }
  }

  public static <PropT> Formula<PropT> Eventually (Formula<PropT> f) {
    // necessary for Java’s type inference to do its work 
    Formula<PropT> tmp = True();
    return unique(new Formula<PropT>(Content.UNTIL, tmp, f, null));
  }

  public static <PropT> Formula<PropT> False () {
    return unique(new Formula<PropT>(Content.FALSE, null, null, null));
  }

  public static <PropT> Formula<PropT> Implies (Formula<PropT> sx, Formula<PropT> dx) {
    return Or(Not(sx), dx);
  }

  public static <PropT> Formula<PropT> Next (Formula<PropT> f) {
    return unique(new Formula<PropT>(Content.NEXT, f, null, null));
  }

  public static <PropT> Formula<PropT> Not (Formula<PropT> f) {
    if (f.is_literal ()) {
      switch (f.content) {
      case TRUE:
        return False();
      case FALSE:
        return True();
      case NOT:
        return f.left;
      default:
        return unique(new Formula<PropT>(Content.NOT, f, null, null));
      }
    }

    // f is not a literal, so go on...
    // The methods used here call unique() themselves.
    switch (f.content) {
    case AND:
      return Or(Not(f.left), Not(f.right));
    case OR:
      return And(Not(f.left), Not(f.right));
    case UNTIL:
      return Release(Not(f.left), Not(f.right));
    case RELEASE:
      return Until(Not(f.left), Not(f.right));
    case WEAK_UNTIL:
      return WRelease(Not(f.left), Not(f.right));
    //case 'M': return WUntil(Not(f.left), Not(f.right));
    case NOT:
      return f.left;
    case NEXT:
      return Next(Not(f.left));
    default:
      assert false : "found literal with is_literal() false";
      return null;
    }
  }

  public static <PropT> Formula<PropT> Or (Formula<PropT> sx, Formula<PropT> dx) {
    if (sx.id < dx.id) {
      return unique(new Formula<PropT>(Content.OR, sx, dx, null));
    } else {
      return unique(new Formula<PropT>(Content.OR, dx, sx, null));
    }
  }

  public static <PropT> Formula<PropT> Proposition (PropT name) {
    return unique(new Formula<PropT>(Content.PROPOSITION, null, null, name));
  }

  public static <PropT> Formula<PropT> Release (Formula<PropT> sx, Formula<PropT> dx) {
    return unique(new Formula<PropT>(Content.RELEASE, sx, dx, null));
  }

  public static <PropT> Formula<PropT> True () {
    return unique(new Formula<PropT>(Content.TRUE, null, null, null));
  }

  public static <PropT> Formula<PropT> Until (Formula<PropT> sx, Formula<PropT> dx) {
    return unique(new Formula<PropT>(Content.UNTIL, sx, dx, null));
  }

  public static <PropT> Formula<PropT> WRelease (Formula<PropT> sx, Formula<PropT> dx) {
    return unique(new Formula<PropT>(Content.UNTIL, dx, And(sx, dx), null));
  }

  public static <PropT> Formula<PropT> WUntil (Formula<PropT> sx, Formula<PropT> dx) {
    return unique(new Formula<PropT>(Content.WEAK_UNTIL, sx, dx, null));
  }

  private static void clearCache() {
    cache = new HashSet<Formula<?>>();
  }

  @SuppressWarnings ("unchecked")
  private static <PropT> Formula<PropT> unique (Formula<PropT> f) {
    for (Formula<?> g: cache) {
      if (f.equals (g))
        return (Formula<PropT>)g;
    }
    cache.add ((Formula<Object>)f);
    return f;
  }
  
  @Override
  public boolean equals (Object obj) {
    if (obj == null || !(obj instanceof Formula<?>))
      return false;
    Formula<?> f = (Formula<?>)obj;
    switch (content) {
    case PROPOSITION:
      return name.equals (f.name);
    case AND:
    case OR:
    case UNTIL:
    case RELEASE:
    case WEAK_UNTIL:
      return f.content == content &&
        left.equals (f.left) && right.equals (f.right);
    case NOT:
    case NEXT:
      return f.content == content && left.equals (f.left);
    case TRUE:
    case FALSE:
      return f.content == content;
    default:
      assert false : "unknown operator";
    }
    return false;
  }
}
