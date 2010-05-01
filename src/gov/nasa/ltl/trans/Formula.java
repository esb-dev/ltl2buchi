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
public class Formula implements Comparable<Formula> {
  private static int       nId = 0;
  private static Hashtable<String, Formula> ht = new Hashtable<String, Formula>();
  private static Hashtable<String, Formula> matches = new Hashtable<String, Formula>();
  private char             content;
  private boolean          literal;
  private Formula          left;
  private Formula          right;
  private int              id;
  private int              untils_index; // index to the untils vector
  private BitSet           rightOfWhichUntils; // for bug fix - formula can be right of >1 untils
  private String           name;
  private boolean          has_been_visited;

  private Formula (char c, boolean l, Formula sx, Formula dx, String n) {
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

  public String getName () {
    return name;
  }

  public Formula getNext () {
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

  public Formula getSub1 () {
    if (content == 'V') {
      return right;
    } else {
      return left;
    }
  }

  public Formula getSub2 () {
    if (content == 'V') {
      return left;
    } else {
      return right;
    }
  }

  public void addLeft (Formula l) {
    left = l;
  }

  public void addRight (Formula r) {
    right = r;
  }

  public int compareTo (Formula f) {
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

  public boolean is_special_case_of_V (TreeSet<Formula> check_against) {
    Formula form = (Release(False(), this));

    if (check_against.contains(form)) {
      return true;
    } else {
      return false;
    }
  }

  public boolean is_synt_implied (TreeSet<Formula> old, TreeSet<Formula> next) {
    if (this.getContent() == 't') {
      return true;
    }

    if (old.contains(this)) {
      return true;
    }

    if (!is_literal()) // non-elementary formula
    {
      Formula form1 = this.getSub1();
      Formula form2 = this.getSub2();
      Formula form3 = this.getNext();

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

  public Formula negate () {
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

  public Formula rewrite (Formula rule, Formula rewritten) {
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
      Formula expr = rewritten.rewrite();

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

  static Formula Always (Formula f) {
    return unique(new Formula('V', false, False(), f, null));
  }

  static Formula And (Formula sx, Formula dx) {
    if (sx.id < dx.id) {
      return unique(new Formula('A', false, sx, dx, null));
    } else {
      return unique(new Formula('A', false, dx, sx, null));
    }
  }

  static Formula Eventually (Formula f) {
    return unique(new Formula('U', false, True(), f, null));
  }

  static Formula False () {
    return unique(new Formula('f', true, null, null, null));
  }

  static Formula Implies (Formula sx, Formula dx) {
    return Or(Not(sx), dx);
  }

  static Formula Next (Formula f) {
    return unique(new Formula('X', false, f, null, null));
  }

  static Formula Not (Formula f) {
    if (f.literal) {
      switch (f.content) {
      case 't':
        return False();

      case 'f':
        return True();

      case 'N':
        return f.left;

      default:
        return unique(new Formula('N', true, f, null, null));
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

  static Formula Or (Formula sx, Formula dx) {
    if (sx.id < dx.id) {
      return unique(new Formula('O', false, sx, dx, null));
    } else {
      return unique(new Formula('O', false, dx, sx, null));
    }
  }

  static Formula Proposition (String name) {
    return unique(new Formula('p', true, null, null, name));
  }

  static Formula Release (Formula sx, Formula dx) {
    return unique(new Formula('V', false, sx, dx, null));
  }

  static Formula True () {
    return unique(new Formula('t', true, null, null, null));
  }

  static Formula Until (Formula sx, Formula dx) {
    return unique(new Formula('U', false, sx, dx, null));
  }

  static Formula WRelease (Formula sx, Formula dx) {
    return unique(new Formula('U', false, dx, And(sx, dx), null));
  }

  static Formula WUntil (Formula sx, Formula dx) {
    return unique(new Formula('W', false, sx, dx, null));
  }

  private static void clearHT () {
    ht = new Hashtable<String, Formula>();
  }

  private static void clearMatches () {
    matches = new Hashtable<String, Formula>();
  }

  private static Formula unique (Formula f) {
    String s = f.toString();

    if (ht.containsKey(s)) {
      return ht.get(s);
    }

    ht.put(s, f);

    return f;
  }

  private Formula getMatch (String name) {
    return matches.get(name);
  }

  private void addMatch (String name, Formula expr) {
    matches.put(name, expr);
  }

  private boolean match (Formula rule) {
    if (rule.content == 'p') {
      Formula match = getMatch(rule.name);

      if (match == null) {
        addMatch(rule.name, this);

        return true;
      }

      return match == this;
    }

    if (rule.content != content) {
      return false;
    }

    Hashtable<String, Formula> saved = new Hashtable<String, Formula>(matches);

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

  private Formula rewrite () {
    if (content == 'p') {
      return getMatch(name);
    }

    switch (content) {
    case 'A':
      return And(left.rewrite(), right.rewrite());

    case 'O':
      return Or(left.rewrite(), right.rewrite());

    case 'U':
      return Until(left.rewrite(), right.rewrite());

    case 'V':
      return Release(left.rewrite(), right.rewrite());

    case 'W':
      return WUntil(left.rewrite(), right.rewrite());

    case 'X':
      return Next(left.rewrite());

    case 'N':
      return Not(left.rewrite());

    case 't':
      return True();

    case 'f':
      return False();
    }

    throw new RuntimeException("code should not be reached");
  }
}
