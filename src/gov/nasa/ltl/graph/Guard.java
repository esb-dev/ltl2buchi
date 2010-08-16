/**
 * 
 */
package gov.nasa.ltl.graph;

import java.util.Iterator;
import java.util.TreeSet;

/**
 * 
 * …Must implement {@link java.lang.Comparable} because this is used in
 * {@link ITypeNeighbor}. 
 * @author Ewgenij Starostin
 *
 */
public class Guard<PropT> extends TreeSet<Literal<PropT>>
  implements Comparable<Guard<PropT>>{
  private static final long serialVersionUID = -6366863384153872811L;
  /**
   * Check if this instance is a subterm of g. This is the case iff
   * every literal of this instance is present in g. We expect PropT
   * to know whether two PropT instances are the same atom.
   * If this instance has size 0, it is a subterm of any other
   * instance.
   * @param g
   * @return
   */
  public boolean subtermOf (Guard<PropT> g) {
    return g.containsAll (this);
  }
  
  /**
   * The order used here doesn’t have any particular
   * meaning.
   */
  public int compareTo (Guard<PropT> o) {
    if (o == null)
      throw new NullPointerException();
    if (equals (o))
      return 0;
    if (size() < o.size ())
      return -1;
    if (size() > o.size ())
      return 1;
    Iterator<Literal<PropT>> i = o.iterator ();
    for (Literal<PropT> l: this) {
      assert i.hasNext ();
      Literal<PropT> k = i.next ();
      int r = l.compareTo (k);
      if (r < 0)
        return -1;
      if (r > 0)
        return 1;
    }
    return 0;
  }

  /**
   * Synonym for {@link #isEmpty()}.
   * @return
   */
  public boolean isTrue () {
    return isEmpty ();
  }
}
