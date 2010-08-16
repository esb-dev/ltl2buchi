/**
 * 
 */
package gov.nasa.ltl.graph;

import java.util.ArrayList;

/**
 * @author Ewgenij Starostin
 *
 */
public class Literal<PropT> implements Comparable<Literal<PropT>> {
  private PropT atom;
  private boolean negated;
  
  public Literal(PropT atom, boolean negated) {
    assert atom != null;
    this.atom = atom;
    this.negated = negated;
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

  @Override
  public int compareTo (Literal<PropT> o) {
    if (o == null)
      throw new NullPointerException ();
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
      (obj == this || compareTo ((Literal<PropT>)obj) == 0);
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
   * 
   * The representatives list takes arbitrary objects, in case we ever
   * use Literal over different PropT classes concurrently.
   */
  private static ArrayList<Object> representatives = new ArrayList<Object>();
  
  @SuppressWarnings ("unchecked")
  private int compareAtoms (PropT a1, PropT a2) {
    if (a1 instanceof Comparable<?> && a2 instanceof Comparable<?>) {
      Comparable<PropT> c1 = (Comparable<PropT>)a1,
                        c2 = (Comparable<PropT>)a2;
      // paranoid symmetry check
      int r1 = c1.compareTo (a2), r2 = c2.compareTo (a1);
      if (r1 == -r2)
        return r1;
    }
    if (a1.equals (a2))
      return 0;
    // else, a1 and a2 are not equivalent
    for (Object o: representatives) { // iterator is ordered
      if (a1.equals (o))
        return -1;
      if (a2.equals (o))
        return 1;
    }
    /* If we get here, neither atom has been recorded yet and we know
     * they are not equivalent, so make two new equivalence classes.
     */
    representatives.add (a1);
    representatives.add (a2);
    return -1;
  }
}
