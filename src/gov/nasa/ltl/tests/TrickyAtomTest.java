package gov.nasa.ltl.tests;

import gov.nasa.ltl.graph.Graph;
import gov.nasa.ltl.graphio.Writer;
import gov.nasa.ltl.trans.Formula;
import gov.nasa.ltl.trans.LTL2Buchi;

/**
 * This class tests formula instantiation and transformation
 * over an atom type which has a mutable internal state and
 * toString representation. The first part of the output is
 * for a simpler atom type; the outputs for both parts should
 * be isomorphic. The formula used here is:
 * []((<>a)/\(a-><>b)/\(b-><>c))
 * @author estar
 *
 */
public class TrickyAtomTest {
  private static class TrickyAtom implements Comparable<TrickyAtom> {
    private char x;
    private static int y = 0;

    public TrickyAtom (char c) {
      x = c;
      y++;
    }

    public String toString () {
      return "{" + x + "," + (y++) + "}";
    }

    @Override
    public int compareTo (TrickyAtom o) {
      y++;
      return (new Character (x)).compareTo (o.x);
    }

    @Override
    public boolean equals (Object o) {
      y++;
      return o != null && o instanceof TrickyAtom
          && compareTo ((TrickyAtom) o) == 0;
    }
  }

  public static void main (String[] args) {
    LTL2Buchi.debug = true;
    System.out.println ("Easy:");
    Formula<Character> easy = Formula.Always (Formula.And (Formula.And (Formula
        .Eventually (Formula.Proposition ('a')), Formula
        .Implies (Formula.Proposition ('a'), Formula
            .Eventually (Formula.Proposition ('b')))), Formula
        .Implies (Formula.Proposition ('b'), Formula
            .Eventually (Formula.Proposition ('c')))));
    System.out.println (easy);
    Graph<Character> gEasy = LTL2Buchi.translate (easy);
    Writer<Character> v = Writer.getWriter (Writer.Format.FSP, System.out);
    v.write (gEasy);
    
    Formula.reset_static ();
    
    System.out.println("Tricky:");
    Formula<TrickyAtom> tricky = Formula.Always (Formula.And (Formula.And (Formula
        .Eventually (Formula.Proposition (new TrickyAtom ('a'))), Formula
        .Implies (Formula.Proposition (new TrickyAtom ('a')), Formula
            .Eventually (Formula.Proposition (new TrickyAtom ('b'))))), Formula
        .Implies (Formula.Proposition (new TrickyAtom ('b')), Formula
            .Eventually (Formula.Proposition (new TrickyAtom ('c'))))));
    System.out.println (tricky);
    Graph<TrickyAtom> gTricky = LTL2Buchi.translate (tricky);
    Writer<TrickyAtom> w = Writer.getWriter (Writer.Format.FSP, System.out);
    w.write (gTricky);
  }
}
