/**
 * 
 */
package gov.nasa.ltl.graph;

import java.util.ArrayList;

/**
 * @author estar
 *
 */
public class Literal<PropT> implements Comparable<Literal<PropT>> {
  private PropT atom;
  private boolean negated;
  private boolean isTrue;
  
  public Literal(PropT atom, boolean negated, boolean isTrue) {
    assert (atom != null && !isTrue) ||
      (atom == null && !negated && isTrue);
    this.atom = atom;
    this.negated = negated;
    this.isTrue = isTrue;
  }

  /**
   * @return the atom
   */
  public PropT getAtom () {
    return atom;
  }

  /**
   * @return the negated
   */
  public boolean isNegated () {
    return negated;
  }

  /**
   * @return the isTrue
   */
  public boolean isTrue () {
    return isTrue;
  }

  /**
   * null < TRUE < ... < x < !x < ...
   */
  @Override
  public int compareTo (Literal<PropT> o) {
    if (o == null)
      return 1;
    if (isTrue)
      if (o.isTrue)
        return 0;
      else
        return -1;
    int r = compareAtoms (atom, o.atom);
    if (r != 0)
      return r;
    if (negated)
      if (o.negated)
        return 0;
      else
        return 1;
    else
      if (o.negated)
        return -1;
      else
        return 0;
  }
  
  @SuppressWarnings ("unchecked")
  @Override
  public boolean equals (Object obj) {
    return obj != null && obj instanceof Literal<?> &&
      compareTo ((Literal<PropT>)obj) == 0;
  }
  
  /* We permit arbitrary atoms. Unfortunately, this means we can’t
   * generally compare them. However, to store them efficiently,
   * this’d be nice to have. So what follows is a fallback comparison
   * method for this case, but we prefer the proper compareTo method
   * if the atoms happen to be Comparable.
   * 
   * TODO: For this to work, we assume that the equals method satisfies
   * its contract; document this somewhere...
   * We also assume that if PropT does not implement Comparable, then
   * neither do any subclasses passed into this class.
   * 
   * We keep a list of representatives of equivalence classes of atoms
   * seen so far and assign them (arbitrary) numbers. This induces a
   * an ordering on the quotient of the atom type over its equals method
   * and from there an ordering of the atoms.
   */
  
  private ArrayList<PropT> representatives = new ArrayList<PropT>();
  
  @SuppressWarnings ("unchecked")
  private int compareAtoms (PropT a1, PropT a2) {
    if (a1 instanceof Comparable<?> && a2 instanceof Comparable<?>)
      return ((Comparable<PropT>)a1).compareTo (a2);
    int r1 = -1, r2 = -1;
    if (a1.equals (a2))
      return 0;
    // else, a1 and a2 are not equivalent
    for (int i = 0; i < representatives.size (); i++) {
      if (a1.equals (representatives.get (i))) {
        assert r1 == -1 : "" + a1.getClass () +
          " has non-transitive equals(), expect problems";
        r1 = i;
        if (r2 == -1) // we know a2 is not in this equivalence class
          return -1;
      }
      if (a2.equals (representatives.get (i))) {
        assert r2 == -1 : "" + a2.getClass () +
          " has non-transitive equals(), expect problems";
        r2 = i;
        if (r1 == -1)
          return 1;
      }
    }
    /* If we get here, neither atom has been recorded yet and we know
     * they are not equivalent, so make two new equivalence classes.
     */
    representatives.add (a1);
    representatives.add (a2);
    return -1;
  }
}
