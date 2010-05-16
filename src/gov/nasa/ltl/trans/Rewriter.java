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

import java.io.*;

// Added by ckong - Sept 7, 2001
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;


/**
 * This class provides the means to rewriting a formula according to
 * some set of simplification rules. A rule is a pair of
 * {@link Formula}&lt;String&gt; instances, with the atoms of the first
 * formula being used as variables.
 * 
 * Rules are read at class loading time from the file given by the
 * <code>gov.nasa.ltl.trans.rules</code> property, if it is set, and
 * by {@link RulesClass#getRules()} otherwise.
 * 
 * Rules are applied until no rule matches. This class does not ensure
 * that the process is finite, so the rules have to be chosen carefully.
 */
public class Rewriter<PropT> {
  private static Formula<String>[] rules;
  static {
    rules = null;
    readRules (null);
  }
  
  private Formula<PropT> formula;
  private Hashtable<String, Formula<PropT>> matches;
  /* Make a snapshot of the rules in case the static ones change
   * during rewriting.
   */
  private Formula<String>[] instanceRules =
    Arrays.copyOf (rules, rules.length);
  /* Keep track of (sub)formulae we’ve already rewritten.
   * The container class is so that we compare instances directly
   * instead of checking the formula structure.
   */
  private class FormulaContainer {
    private final Formula<PropT> f;
    public FormulaContainer(Formula<PropT> fo) { f = fo; }
    @Override public int hashCode () { return f.getId (); }
    @Override public boolean equals (Object obj) {
      return obj != null && obj instanceof Rewriter<?>.FormulaContainer 
        && hashCode () == obj.hashCode ();}
  }
  private HashSet<FormulaContainer> rewritten;
  
  /**
   * Create a rewriter for the given formula, which might be modified
   * by {@link #rewrite()}.
   * @param f
   */
  public Rewriter (Formula<PropT> f) {
    formula = f;
    rewritten = new HashSet<FormulaContainer> ();
  }

  /**
   * Initialise an instance for a subformula.
   * @param f subformula
   * @param r set of formulae which have already been rewritten by
   *    {@link Rewriter} ancestors of this instance
   */
  protected Rewriter (Formula<PropT> f, HashSet<FormulaContainer> r) {
    formula = f;
    rewritten = r;
  }

  /**
   * Rewrite this instance’s formula according to the loaded rules. That
   * formula might be modified in the process. This method is not
   * guaranteed to terminate unless the rules are chosen suitably
   * (e. g. only shortening and/or only away from some operators).
   * @return instance of simplified formula; this is usually not
   *    the same instance which was passed to the constructor.
   */
  public Formula<PropT> rewrite () {
    boolean negated = false;
    boolean changed;

    assert rules != null : "rules not loaded";
    if (formula.isLiteral () || isRewritten (formula)) {
      return formula;
    }
    do {
      changed = false;
      for (int i = 0; i + 1 < instanceRules.length; i += 2)
        if (rewrite (instanceRules[i], instanceRules[i + 1]))
          changed = true;
      negated = !negated;
      formula = Formula.Not (formula);
    } while (changed || negated);
    markRewritten (formula);
    return formula;
  }
  
  /**
   * Check for a given formula whether it is the end product
   * of rewriting.
   * @param f
   * @return
   */
  private boolean isRewritten (Formula<PropT> f) {
    return rewritten.contains (new FormulaContainer (f));
  }
  
  /**
   * Mark a formula as the end product of rewriting.
   * @param f
   */
  private void markRewritten (Formula<PropT> f) {
    rewritten.add (new FormulaContainer (f));
  }

  /**
   * Attempt to apply a rewrite rule to this rewriter’s formula.
   * @param rule top half of the rule
   * @param target bottom half of the rule
   * @return true if the rule was applied, false else
   */
  private boolean rewrite (Formula<String> rule, Formula<String> target) {
    Formula<PropT> f1 = formula.getSub1 (), f2 = formula.getSub2 ();
    if (f1 != null)
      f1 = new Rewriter<PropT> (f1, rewritten).rewrite ();
    if (f2 != null)
      f2 = new Rewriter<PropT> (f2, rewritten).rewrite ();
    switch (formula.getContent ()) {
    case AND:
    case OR:
    case UNTIL:
    case WEAK_UNTIL:
      formula.addLeft (f1);
      formula.addRight (f2);
      break;
    case RELEASE: // Sub1, Sub2 are swapped in this case
      formula.addRight (f1);
      formula.addLeft (f2);
      break;
    case NEXT:
    case NOT:
      formula.addLeft (f1);
      break;
    case TRUE:
    case FALSE:
    case PROPOSITION:
      return false;
    }
    matches = new Hashtable<String, Formula<PropT>> ();
    if (match (formula, rule)) {
      formula = substituteMatches (target);
      return true;
    }
    return false;
  }

  /**
   * Attempt to match this instance’s formula against the first part of
   * a rewrite rule by checking that they have the same structure and
   * by adding variable name/formula pairs to {@link #matches}.
   * This method expects {@link #matches} to be empty when
   * called from another method.
   * @param f (sub)formula to match against a rule
   * @param rule (sub)formula of the rule pattern
   * @return true if the subformula matched the pattern; in this case,
   *    {@link #matches} contains the necessary variable values.
   *    False else.
   */
  private boolean match (Formula<PropT> f, Formula<String> rule) {
    Formula<PropT> match;
    Hashtable<String, Formula<PropT>> saved; 

    if (rule.getContent () != f.getContent ()) {
      return false;
    }
    saved = new Hashtable<String, Formula<PropT>> (matches);
    switch (f.getContent ()) {
    case PROPOSITION:
      match = (Formula<PropT>)matches.get (rule.getName ());
      if (match == null) {
        matches.put (rule.getName (), f);
        return true;
      }
      return match == f;
    case AND:
    case OR:
      if (match (f.getSub1 (), rule.getSub1 ()) &&
          match(f.getSub2 (), rule.getSub2 ())) {
        return true;
      }
      matches = saved;
      if (match (f.getSub2 (), rule.getSub1 ()) &&
          match(f.getSub1 (), rule.getSub2 ())) {
        return true;
      }
      matches = saved;
      return false;
    case UNTIL:
    case RELEASE:
    case WEAK_UNTIL:
      if (match (f.getSub1 (), rule.getSub1 ()) &&
          match(f.getSub2 (), rule.getSub2 ())) {
        return true;
      }
      matches = saved;
      return false;
    case NEXT:
    case NOT:
      if (match (f.getSub1 (), rule.getSub1 ())) {
        return true;
      }
      matches = saved;
      return false;
    case TRUE:
    case FALSE:
      return true;
    default: // can’t happen
      return false;
    }
  }

  /**
   * Build a formula by replacing variables in the second part of a rule
   * by the corresponding values from {@link #matches}, which has been
   * initialised by a prior {@link #match(Formula, Formula)} call with
   * the first part of the same rule.
   * @param f second part of the rule
   * @return formula after rule application
   */
  private Formula<PropT> substituteMatches (Formula<String> f) {
    Formula<PropT> r = null, s, t;
    // This is a bit verbose, to make type inference happen.
    switch (f.getContent ()) {
    case PROPOSITION:
      r = (Formula<PropT>)matches.get (f.getName ());
      break;
    case AND:
      s = substituteMatches (f.getSub1 ());
      t = substituteMatches (f.getSub2 ());
      r = Formula.And(s, t);
      break;
    case OR:
      s = substituteMatches (f.getSub1 ());
      t = substituteMatches (f.getSub2 ());
      r = Formula.Or(s, t);
      break;
    case UNTIL:
      s = substituteMatches (f.getSub1 ());
      t = substituteMatches (f.getSub2 ());
      r = Formula.Until(s, t);
      break;
    case RELEASE:
      s = substituteMatches (f.getSub1 ());
      t = substituteMatches (f.getSub2 ());
      r = Formula.Release(t, s); // because left/right had been switched
      break;
    case WEAK_UNTIL:
      s = substituteMatches (f.getSub1 ());
      t = substituteMatches (f.getSub2 ());
      r = Formula.WUntil(s, t);
      break;
    case NEXT:
      s = substituteMatches (f.getSub1 ());
      r = Formula.Next(s);
      break;
    case NOT:
      s = substituteMatches (f.getSub1 ());
      r = Formula.Not(s);
      break;
    case TRUE:
      r = Formula.True();
      break;
    case FALSE:
      r = Formula.False();
    }
    return r;
  }

  /**
   * Read a set of rewrite rules from a file or from {@link RulesClass}
   * and replace the current set of rules, if any.
   * If the given filename is not null, it is used. Else, if the
   * property <code>gov.nasa.ltl.trans.rules</code> is set, it is used.
   * Else, the rules are obtained from {@link RulesClass#getRules()}.
   * @param filename
   */
  @SuppressWarnings ("unchecked")
  public static void readRules (String filename) {
    String rulesPath = filename != null ? filename :
      System.getProperty ("gov.nasa.ltl.trans.rules");
    BufferedReader in = null;
    LinkedList<Formula<String>> rulesList = new LinkedList<Formula<String>> ();

    try {
      if (rulesPath != null) {
        FileReader fr = new FileReader (rulesPath);
        in = new BufferedReader (fr);
      } else
        in = new BufferedReader (new StringReader (RulesClass.getRules ()));
    } catch (FileNotFoundException e) {
      System.err.println ("Rules file " + rulesPath + " not found.");
      System.exit (1);
    }
    // Modified by ckong - Sept 7, 2001
    while (true) {
      String line = null;
      Formula<String> rule = null;
      
      try {
        line = in.readLine();
      } catch (IOException e) {
        System.err.println ("Failed read from " + in);
        System.exit (1);
      }
      if (line == null)
        break;
      if (line.equals (""))
        continue;
      try {
        rule = Parser.parse (line);
      } catch (ParseErrorException e) {
        System.err.println ("Exception while reading rules: " + e);
        System.exit (1);
      }
      rulesList.add (rule);
    }
    rules = (Formula<String>[]) rulesList.toArray (new Formula[]{});
  }
}