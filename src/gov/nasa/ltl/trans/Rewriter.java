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
import java.io.StringReader;
import java.util.Hashtable;


/**
 * DOCUMENT ME!
 */
public class Rewriter<PropT> {
  private static Formula<String>[] rules;
  static {
    rules = null;
    readRules ();
  }
  
  private Formula<PropT> formula;
  private Hashtable<String, Formula<PropT>> matches;
  
  public Rewriter(Formula<PropT> f) {
    formula = f;
  }
  
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
   * Attempt to apply a rewrite rule to this rewriter’s formula.
   * @param rule top half of the rule
   * @param rewritten bottom half of the rule
   * @return true if the rule was applied, false else
   * @throws ParseErrorException if the rule was malformed
   */
  private boolean rewrite (Formula<String> rule, Formula<String> rewritten)
    throws ParseErrorException {
    switch (formula.getContent ()) {
    case AND:
    case OR:
    case UNTIL:
    case WEAK_UNTIL:
      formula.addLeft (new Rewriter<PropT> (formula.getSub1 ()).rewrite ());
      formula.addRight (new Rewriter<PropT> (formula.getSub2 ()).rewrite ());
      break;
    case RELEASE:
      formula.addRight (new Rewriter<PropT> (formula.getSub1 ()).rewrite ());
      formula.addLeft (new Rewriter<PropT> (formula.getSub2 ()).rewrite ());
      break;
    case NEXT:
    case NOT:
      formula.addLeft (new Rewriter<PropT> (formula.getSub1 ()).rewrite ());
      break;
    case TRUE:
    case FALSE:
    case PROPOSITION:
      return false;
    }
    matches = new Hashtable<String, Formula<PropT>> ();
    if (match (formula, rule)) {
      formula = substituteMatches (rewritten);
      return true;
    }
    return false;
  }

  public static void main (String[] args) {
    int osize = 0;
    int rsize = 0;

    try {
      if (args.length != 0) {
        for (int i = 0; i < args.length; i++) {
          Formula<String> f = Parser.parse(args[i]);

          osize += f.size();
          System.out.println(f = new Rewriter<String> (f).rewrite());
          rsize += f.size();

          System.err.println(((rsize * 100) / osize) + "% (" + osize + 
                             " => " + rsize + ")");
        }
      } else {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
          try {
            String line = in.readLine();

            if (line == null) {
              break;
            }

            if (line.equals("")) {
              continue;
            }

            Formula<String> f = Parser.parse(line);

            osize += f.size();
            System.out.println(f = new Rewriter<String> (f).rewrite ());
            rsize += f.size();

            System.err.println(((rsize * 100) / osize) + "% (" + osize + 
                               " => " + rsize + ")");
          } catch (IOException e) {
            System.out.println("error");

            break;
          }
        }
      }
    } catch (ParseErrorException e) {
      System.err.println("parse error: " + e.getMessage());
    }
  }

  @SuppressWarnings ("unchecked")
  public static void readRules () {
    rules = new Formula[0];

    try {
      // Modified by ckong - Sept 7, 2001

      /*
         FileReader fr = null;
         
               for(int i = 0, l = ClassPath.length(); i < l; i++)
           try {
             fr = new FileReader(ClassPath.get(i) + File.separator + "gov.nasa.ltl.trans.rules".replace('.', File.separatorChar));
           } catch(FileNotFoundException e) {
           }
         
               if(fr == null) {
           try {
             fr = new FileReader("rules");
           } catch(FileNotFoundException e) {
           }
               }
         
               if(fr == null) return null;
               
               BufferedReader in = new BufferedReader(fr);
       */
      BufferedReader in = new BufferedReader(
                                new StringReader(RulesClass.getRules()));

      while (true) {
        String line = in.readLine();

        if (line == null) {
          break;
        }

        if (line.equals("")) {
          continue;
        }

        Formula<String>   rule = Parser.parse(line);

        Formula<String>[] n = (Formula<String>[])new Formula[rules.length + 1];
        System.arraycopy(rules, 0, n, 0, rules.length);
        n[rules.length] = rule;
        rules = n;
      }
    } catch (IOException e) {
    } catch (ParseErrorException e) {
      System.err.println("parse error: " + e.getMessage());
      System.exit(1);
    }
  }

  public Formula<PropT> rewrite ()
    throws ParseErrorException {
    boolean negated = false;
    boolean changed;

    if (rules == null)
      return formula;
    if (formula.is_literal () || formula.isRewritten ()) {
      formula.setRewritten ();
      return formula;
    }
    do {
      changed = false;
      for (int i = 0; i + 1 < rules.length; i += 2)
        if (rewrite (rules[i], rules[i + 1]))
          changed = true;
      negated = !negated;
      formula = Formula.Not (formula);
    } while (changed || negated);
    formula.setRewritten ();
    return formula;
  }
}