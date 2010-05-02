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

/*
 * TODO: factor the unification algorithm built around the matches
 * table out into a separate class, maybe along with the rewriting
 * code.
 */
/**
 * DOCUMENT ME!
 */
public class Formula<PropT> implements Comparable<Formula<PropT>> {
  private static int       nId = 0;
  /* TODO: Using Object instead of Formula<PropT> for ht and
   * matches means a loss of compile-time type safety, but it’s not
   * possible to use the type parameter for these static variables.
   * Should find a better way to do these things.
   * 
   * These declarations are intended like this:
   * private static Hashtable<String, Formula<?>> ht = ...;
   * private static Hashtable<String, Formula<?>> matches = ...;
   * where ? is PropT.
   */
  private static Hashtable<String, Object> ht =
    new Hashtable<String, Object>();
  private static Hashtable<String, Object> matches =
    new Hashtable<String, Object>();
  private char             content;
  private boolean          literal;
  private Formula<PropT>   left;
  private Formula<PropT>   right;
  private int              id;
  private int              untils_index; // index to the untils vector
  private BitSet           rightOfWhichUntils; // for bug fix - formula can be right of >1 untils
  private PropT            name;
  private boolean          has_been_visited;

  private Formula (char c, boolean l, Formula<PropT> sx, Formula<PropT> dx, PropT n) {
    id = nId++;
    content = c;
    literal = l;
    left = sx;
    right = dx;
    name = n;
    rightOfWhichUntils = null;
    untils_index = -1;
    has_been_visited = false;
  }

  public static boolean is_reserved_char (char ch) {
    switch (ch) {
    //		case 't':
    //		case 'f':
    case 'U':
    case 'V':
    case 'W':
    case 'M':
    case 'X':
    case ' ':
    case '<':
    case '>':
    case '(':
    case ')':
    case '[':
    case ']':
    case '-':

      // ! not allowed by Java identifiers anyway - maybe some above neither?
      return true;

    default:
      return false;
    }
  }

  public static void reset_static () {
    clearMatches();
    clearHT();
  }

  public char getContent () {
    return content;
  }

  public PropT getName () {
    return name;
  }

  public Formula<PropT> getNext () {
    switch (content) {
    case 'U':
    case 'W':
      return this;

    case 'V':
      return this;

    case 'O':
      return null;

    default:

      //    System.out.println(content + " Switch did not find a relevant case...");
      return null;
    }
  }

  public Formula<PropT> getSub1 () {
    if (content == 'V') {
      return right;
    } else {
      return left;
    }
  }

  public Formula<PropT> getSub2 () {
    if (content == 'V') {
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

    if (getContent() == 'U') {
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
    return literal;
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
    if (this.getContent() == 't') {
      return true;
    }

    if (old.contains(this)) {
      return true;
    }

    if (!is_literal()) // non-elementary formula
    {
      Formula<PropT> form1 = this.getSub1();
      Formula<PropT> form2 = this.getSub2();
      Formula<PropT> form3 = this.getNext();

      boolean condition1;
      boolean condition2;
      boolean condition3;

      if (form2 != null) {
        condition2 = form2.is_synt_implied(old, next);
      } else {
        condition2 = true;
      }

      if (form1 != null) {
        condition1 = form1.is_synt_implied(old, next);
      } else {
        condition1 = true;
      }

      if (form3 != null) {
        if (next != null) {
          condition3 = next.contains(form3);
        } else {
          condition3 = false;
        }
      } else {
        condition3 = true;
      }

      switch (getContent()) {
      case 'U':
      case 'W':
      case 'O':
        return (condition2 || (condition1 && condition3));

      case 'V':
        return ((condition1 && condition2) || (condition1 && condition3));

      case 'X':

        if (form1 != null) {
          if (next != null) {
            return (next.contains(form1));
          } else {
            return false;
          }
        } else {
          return true;
        }

      case 'A':
        return (condition2 && condition1);

      default:
        System.out.println("Default case of switch at Form.synt_implied");

        return false;
      }
    } else {
      return false;
    }
  }

  public Formula<PropT> negate () {
    return Not(this);
  }

  public int processRightUntils (int current_index, int acc_sets) {
    has_been_visited = true;

    if (getContent() == 'U') {
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

  public Formula<PropT> rewrite (Formula<String> rule, Formula<String> rewritten) {
    switch (content) {
    case 'A':
    case 'O':
    case 'U':
    case 'V':
    case 'W':
      left = left.rewrite(rule, rewritten);
      right = right.rewrite(rule, rewritten);

      break;

    case 'X':
    case 'N':
      left = left.rewrite(rule, rewritten);

      break;

    case 't':
    case 'f':
    case 'p':
      break;
    }

    if (match(rule)) {
      Formula<PropT> expr = rewrite(rewritten);

      clearMatches();

      return expr;
    }

    clearMatches();

    return this;
  }

  public int size () {
    switch (content) {
    case 'A':
    case 'O':
    case 'U':
    case 'V':
    case 'W':
      return left.size() + right.size() + 1;

    case 'X':
    case 'N':
      return left.size() + 1;

    default:
      return 0;
    }
  }

  public String toString (boolean exprId) {
    if (!exprId) {
      return toString();
    }

    switch (content) {
    case 'A':
      return "( " + left.toString(true) + " /\\ " + right.toString(true) + 
             " )[" + id + "]";

    case 'O':
      return "( " + left.toString(true) + " \\/ " + right.toString(true) + 
             " )[" + id + "]";

    case 'U':
      return "( " + left.toString(true) + " U " + right.toString(true) + 
             " )[" + id + "]";

    case 'V':
      return "( " + left.toString(true) + " V " + right.toString(true) + 
             " )[" + id + "]";

    case 'W':
      return "( " + left.toString(true) + " W " + right.toString(true) + 
             " )[" + id + "]";

    //case 'M': return "( " + left.toString(true) + " M " + right.toString(true) + " )[" + id + "]";
    case 'X':
      return "( X " + left.toString(true) + " )[" + id + "]";

    case 'N':
      return "( ! " + left.toString(true) + " )[" + id + "]";

    case 't':
      return "( true )[" + id + "]";

    case 'f':
      return "( false )[" + id + "]";

    case 'p':
      return "( \"" + name + "\" )[" + id + "]";

    default:
      return "( " + content + " )[" + id + "]";
    }
  }

  public String toString () {
    switch (content) {
    case 'A':
      return "( " + left.toString() + " /\\ " + right.toString() + " )";

    case 'O':
      return "( " + left.toString() + " \\/ " + right.toString() + " )";

    case 'U':
      return "( " + left.toString() + " U " + right.toString() + " )";

    case 'V':
      return "( " + left.toString() + " V " + right.toString() + " )";

    case 'W':
      return "( " + left.toString() + " W " + right.toString() + " )";

    //case 'M': return "( " + left.toString() + " M " + right.toString() + " )";
    case 'X':
      return "( X " + left.toString() + " )";

    case 'N':
      return "( ! " + left.toString() + " )";

    case 't':
      return "( true )";

    case 'f':
      return "( false )";

    case 'p':
      return "( \"" + name + "\" )";

    default:
      return new Character(content).toString();
    }
  }

  public static <PropT> Formula<PropT> Always (Formula<PropT> f) {
    // necessary for Java’s type inference to do its work 
    Formula<PropT> tmp = False();
    return unique(new Formula<PropT>('V', false, tmp, f, null));
  }

  public static <PropT> Formula<PropT> And (Formula<PropT> sx, Formula<PropT> dx) {
    if (sx.id < dx.id) {
      return unique(new Formula<PropT>('A', false, sx, dx, null));
    } else {
      return unique(new Formula<PropT>('A', false, dx, sx, null));
    }
  }

  public static <PropT> Formula<PropT> Eventually (Formula<PropT> f) {
    // necessary for Java’s type inference to do its work 
    Formula<PropT> tmp = True();
    return unique(new Formula<PropT>('U', false, tmp, f, null));
  }

  public static <PropT> Formula<PropT> False () {
    return unique(new Formula<PropT>('f', true, null, null, null));
  }

  public static <PropT> Formula<PropT> Implies (Formula<PropT> sx, Formula<PropT> dx) {
    return Or(Not(sx), dx);
  }

  public static <PropT> Formula<PropT> Next (Formula<PropT> f) {
    return unique(new Formula<PropT>('X', false, f, null, null));
  }

  public static <PropT> Formula<PropT> Not (Formula<PropT> f) {
    if (f.literal) {
      switch (f.content) {
      case 't':
        return False();

      case 'f':
        return True();

      case 'N':
        return f.left;

      default:
        return unique(new Formula<PropT>('N', true, f, null, null));
      }
    }

    // f is not a literal, so go on...
    switch (f.content) {
    case 'A':
      return Or(Not(f.left), Not(f.right));

    case 'O':
      return And(Not(f.left), Not(f.right));

    case 'U':
      return Release(Not(f.left), Not(f.right));

    case 'V':
      return Until(Not(f.left), Not(f.right));

    case 'W':
      return WRelease(Not(f.left), Not(f.right));

    //case 'M': return WUntil(Not(f.left), Not(f.right));
    case 'N':
      return f.left;

    case 'X':
      return Next(Not(f.left));

    default:
      throw new ParserInternalError();
    }
  }

  public static <PropT> Formula<PropT> Or (Formula<PropT> sx, Formula<PropT> dx) {
    if (sx.id < dx.id) {
      return unique(new Formula<PropT>('O', false, sx, dx, null));
    } else {
      return unique(new Formula<PropT>('O', false, dx, sx, null));
    }
  }

  public static <PropT> Formula<PropT> Proposition (PropT name) {
    return unique(new Formula<PropT>('p', true, null, null, name));
  }

  public static <PropT> Formula<PropT> Release (Formula<PropT> sx, Formula<PropT> dx) {
    return unique(new Formula<PropT>('V', false, sx, dx, null));
  }

  public static <PropT> Formula<PropT> True () {
    return unique(new Formula<PropT>('t', true, null, null, null));
  }

  public static <PropT> Formula<PropT> Until (Formula<PropT> sx, Formula<PropT> dx) {
    return unique(new Formula<PropT>('U', false, sx, dx, null));
  }

  public static <PropT> Formula<PropT> WRelease (Formula<PropT> sx, Formula<PropT> dx) {
    return unique(new Formula<PropT>('U', false, dx, And(sx, dx), null));
  }

  public static <PropT> Formula<PropT> WUntil (Formula<PropT> sx, Formula<PropT> dx) {
    return unique(new Formula<PropT>('W', false, sx, dx, null));
  }

  private static void clearHT () {
    ht = new Hashtable<String, Object>();
  }

  private static void clearMatches () {
    matches = new Hashtable<String, Object>();
  }

  @SuppressWarnings ("unchecked")
  private static <PropT> Formula<PropT> unique (Formula<PropT> f) {
    String s = f.toString();

    if (ht.containsKey(s)) {
      return (Formula<PropT>) ht.get(s);
    }

    ht.put(s, (Formula<Object>) f);

    return f;
  }

  @SuppressWarnings ("unchecked")
  private static <PropT> Formula<PropT> getMatch (String name) {
    return (Formula<PropT>) matches.get(name);
  }

  private void addMatch (String name, Formula<PropT> expr) {
    matches.put(name, expr);
  }

  private boolean match (Formula<String> rule) {
    if (rule.content == 'p') {
      Formula<PropT> match = getMatch(rule.name);

      if (match == null) {
        addMatch(rule.name, this);

        return true;
      }

      return match == this;
    }

    if (rule.content != content) {
      return false;
    }

    Hashtable<String, Object> saved = new Hashtable<String, Object>(matches);

    switch (content) {
    case 'A':
    case 'O':

      if (left.match(rule.left) && right.match(rule.right)) {
        return true;
      }

      matches = saved;

      if (right.match(rule.left) && left.match(rule.right)) {
        return true;
      }

      matches = saved;

      return false;

    case 'U':
    case 'V':
    case 'W':

      if (left.match(rule.left) && right.match(rule.right)) {
        return true;
      }

      matches = saved;

      return false;

    case 'X':
    case 'N':

      if (left.match(rule.left)) {
        return true;
      }

      matches = saved;

      return false;

    case 't':
    case 'f':
      return true;
    }

    throw new RuntimeException("code should not be reached");
  }

  private static <PropT> Formula<PropT> rewrite (Formula<String> f) {
    Formula<PropT> r = null, s, t;
    // This is a bit verbose, to make type inference happen.
    switch (f.content) {
    case 'p':
      r = getMatch(f.name);
      break;
    case 'A':
      s = rewrite(f.left);
      t = rewrite(f.right);
      r = And(s, t);
      break;
    case 'O':
      s = rewrite(f.left);
      t = rewrite(f.right);
      r = Or(s, t);
      break;
    case 'U':
      s = rewrite(f.left);
      t = rewrite(f.right);
      r = Until(s, t);
      break;
    case 'V':
      s = rewrite(f.left);
      t = rewrite(f.right);
      r = Release(s, t);
      break;
    case 'W':
      s = rewrite(f.left);
      t = rewrite(f.right);
      r = WUntil(s, t);
      break;
    case 'X':
      s = rewrite(f.left);
      r = Next(s);
      break;
    case 'N':
      s = rewrite(f.left);
      r = Not(s);
      break;
    case 't':
      r = True();
      break;
    case 'f':
      r = False();
    }
    if(r != null)
      return r;
    throw new RuntimeException("code should not be reached");
  }
}
