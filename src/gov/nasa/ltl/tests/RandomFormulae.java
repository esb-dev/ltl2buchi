/**
 * 
 */
package gov.nasa.ltl.tests;

import gov.nasa.ltl.graph.Degeneralize;
import gov.nasa.ltl.graph.Graph;
import gov.nasa.ltl.graph.SCCReduction;
import gov.nasa.ltl.graph.SFSReduction;
import gov.nasa.ltl.graph.Simplify;
import gov.nasa.ltl.graph.SuperSetReduction;
import gov.nasa.ltl.trans.Formula;
import gov.nasa.ltl.trans.LTL2Buchi;
import gov.nasa.ltl.trans.Node;
import gov.nasa.ltl.trans.Pool;
import gov.nasa.ltl.trans.Rewriter;
import gov.nasa.ltl.trans.Translator;

import java.util.Iterator;
import java.util.Random;

/**
 * Implementation of the experiment described in section
 * 6.1 of Dimitra Giannakopoulou and Flavio Lerda 2002,
 * <em>From States to Transitions: Improving Translation of
 * LTL Formulae to Büchi Automata</em>. The data are printed
 * to standard output, the gnuplot commands to display them are:<br />
 * <code>set key autotitle columnheader</code><br />
 * <code>plot [0:<em>L</em>] [0:1] for [i in "2 4 6 8"] "data.file" using 1:i:(column(i+1)) with errorbars</code><br />
 * The parameters of the experiment are set via static variables of
 * this class (for now).
 * @author estar
 *
 */
public class RandomFormulae {
  public static class FormulaGenerator implements Iterator<Formula<Integer>> {
    private int length, names;
    private double pUorV;
    private Random rand;
    
    public FormulaGenerator (int length, int names, double pUorV) {
      this (length, names, pUorV, null);
    }
    
    public FormulaGenerator (int length, int names, double pUorV, Random rand) {
      assert 0 <= pUorV && pUorV <= 1 : "bad probability for U or V";
      assert length > 0 : "length must be positive";
      assert names > 0 : "need at least one name";
      if (rand == null)
        this.rand = new Random ();
      else
        this.rand = rand;
      this.length = length;
      this.names = names;
      this.pUorV = pUorV;
    }

    @Override public boolean hasNext () { return true; }

    @Override
    public Formula<Integer> next () {
      return randomFormula (length);
    }
    
    private Formula<Integer> randomFormula (int length) {
      int name = 0, sublength;
      @SuppressWarnings ("unused")
      Formula<Integer> f = null, s1 = null, s2 = null;
      LTL2Buchi.debug = false;
      switch (length) {
      case 0:
        assert false : "formulae cannot have zero length";
        return null;
      case 1:
        name = rand.nextInt (names);
        return f = Formula.Proposition (name);
      case 2:
        s1 = randomFormula (1);
        if (rand.nextDouble () < 0.5)
          return Formula.Not (s1);
        else
          return Formula.Next (s1);
      default:
        sublength = 1 + rand.nextInt (length - 2);
        if (rand.nextDouble () < pUorV) {
          s1 = randomFormula (sublength);
          s2 = randomFormula (length - sublength - 1);
          if (rand.nextDouble () < 0.5)
            return f = Formula.Until (s1, s2);
          else
            return f = Formula.Release (s1, s2);
        } else {
          if (rand.nextDouble () < 0.5) {
            s1 = randomFormula (length - 1);
            if (rand.nextDouble () < 0.5)
              return Formula.Not (s1);
            else
              return Formula.Next (s1);
          } else {
            s1 = randomFormula (sublength);
            s2 = randomFormula (length - sublength - 1);
            if (rand.nextDouble () < 0.5)
              return f = Formula.And (s1, s2);
            else
              return f = Formula.Or (s1, s2);
          }
        }
      }
    }

    @Override public void remove () {}
  }

  public static class FormulaSource implements Iterable<Formula<Integer>> {
    private FormulaGenerator gen;
    
    public FormulaSource (int length, int names, double pUorV) {
      this (length, names, pUorV, null);
    }

    public FormulaSource (int length, int names, double pUorV, Random rand) {
      gen = new FormulaGenerator (length, names, pUorV, rand);
    }
    
    @Override
    public Iterator<Formula<Integer>> iterator () {
      return gen;
    }
  }
  
  public static int N = 5, Lmin = 5, Lmax = 30, Linc = 5,
    F = 100, seed = 0;
  public static boolean haveSeed = false, optimise = false;
  static double P = 1.0/3.0;

  /**
   * @param args
   */
  public static void main (String[] args) {
    Random rand;
    if (haveSeed)
      rand = new Random (seed);
    else
      rand = new Random ();
    // TODO: parse args to get custom values
    System.out.println ("\"L\" \"GBA states\" \"\" \"GBA transitions\" "+
        "\"\" \"BA states\" \"\" \"BA transitions\" \"\"");
    for (int L = Lmin; L <= Lmax; L += Linc) {
      FormulaSource formulae = new FormulaSource (L, N, P, rand);
      int i = 0;
      double[] baStates = new double[F], baTrans = new double[F],
        gbaStates = new double[F], gbaTrans = new double[F];
      for (Formula<Integer> f: formulae) {
        if (i >= F)
          break;
        Graph<Integer> baAut, gbaAut, baBu, gbaBu;
        if (optimise)
          f = new Rewriter<Integer> (f).rewrite ();
        Translator.set_algorithm (Translator.Algorithm.LTL2AUT);
        Node.reset_static ();
        Pool.reset_static ();
        gbaAut = Translator.translate (f);
        if (optimise)
          gbaAut = SuperSetReduction.reduce (gbaAut);
        baAut = Degeneralize.degeneralize (gbaAut);
        if (optimise)
          baAut = SFSReduction.reduce (
              Simplify.simplify (SCCReduction.reduce (baAut)));
        /* If baAut ends up empty, the formula was unsatisfiable.
         * We’ll skip these.
         */
        if (baAut.getNodeCount () == 0 || baAut.getEdgeCount () == 0) {
          Formula.reset_static ();
          continue;
        }
        // If baAut is non-empty, gbaAut is non-empty too.
        assert gbaAut.getNodeCount () > 0 && gbaAut.getEdgeCount () > 0;
        Translator.set_algorithm (Translator.Algorithm.LTL2BUCHI);
        Node.reset_static ();
        Pool.reset_static ();
        gbaBu = Translator.translate (f);
        if (optimise)
          gbaBu = SuperSetReduction.reduce (gbaBu);
        baBu = Degeneralize.degeneralize (gbaBu);
        if (optimise)
          baBu = SFSReduction.reduce (
              Simplify.simplify (SCCReduction.reduce (baBu)));
        gbaStates[i] = gbaBu.getNodeCount () / (double)gbaAut.getNodeCount ();
        gbaTrans[i] = gbaBu.getEdgeCount () / (double)gbaAut.getEdgeCount ();
        baStates[i] = baBu.getNodeCount () / (double)baAut.getNodeCount ();
        baTrans[i] = baBu.getEdgeCount () / (double)baAut.getEdgeCount ();
        Formula.reset_static ();
        i++;
      }
      output (L, gbaStates, gbaTrans, baStates, baTrans);
    }
  }
  
  private static void output (int L, double[] gbaStates,
      double[] gbaTrans, double[] baStates, double[] baTrans) {
    double gbaStatesM = mean (gbaStates), gbaTransM = mean (gbaTrans),
           baStatesM = mean (baStates), baTransM = mean (baTrans),
           gbaStatesD = sigma (gbaStates, gbaStatesM),
           gbaTransD = sigma (gbaTrans, gbaTransM),
           baStatesD = sigma (baStates, baStatesM),
           baTransD = sigma (baTrans, baTransM);
    System.out.println ("" + L + " " + gbaStatesM + " " +
        gbaStatesD + " " + gbaTransM + " " + gbaTransD + " " +
        baStatesM + " " + baStatesD + " " + baTransM + " " + baTransD);
  }
  
  private static double mean(double[] values) {
    double s = 0;
    for (int i = 0; i < values.length; i++)
      s += values[i];
    return s / values.length;
  }
  
  private static double sigma(double[] values, double mean) {
    double s = 0;
    for (int i = 0; i < values.length; i++)
      s += (values[i] - mean) * (values[i] - mean);
    return s / values.length;
  }
}
