/**
 * 
 */
package gov.nasa.ltl.graph;

import gov.nasa.ltl.trans.Guard;

import java.util.AbstractList;

/**
 * 
 * …Must implement {@link java.lang.Comparable} because this is used in
 * {@link ITypeNeighbor}. 
 * @author estar
 *
 */
public abstract class AbstractGuard<PropT> extends AbstractList<PropT>
  implements Comparable<AbstractGuard<PropT>>{
  public abstract boolean getNeg (int index);
  public abstract boolean getTrue (int index);
  /**
   * Check if this instance is a subterm of g. This is the case iff
   * every literal of this instance is present in g. We expect PropT
   * to know whether two PropT instances are the same atom.
   * If this instance has size 0, it is a subterm of any other
   * instance.
   * @param g
   * @return
   */
  public boolean subtermOf (AbstractGuard<PropT> g) {
    for (int i = 0; i < size (); i++) {
      boolean found = false;
      for (int j = 0; j < g.size (); j++)
        if (((get (i) == null && g.get (j) == null) ||
             (get (i) != null && get (i).equals (g.get (j)))) &&
            getNeg (i) == g.getNeg (j) &&
            getTrue (i) == g.getTrue (j)) {
          found = true;
          break;
        }
      if (!found)
        return false;
    }
    return true;
  }
  
  @SuppressWarnings ("unchecked")
  public boolean equals (Object o) {
    if (o == null || !(o instanceof Guard<?>))
      return false;
    AbstractGuard<PropT> p = (AbstractGuard<PropT>)o;
    return subtermOf (p) && p.subtermOf (this);
  }
  
  /**
   * The order used here doesn’t have any particular
   * meaning.
   */
  public int compareTo (AbstractGuard<PropT> o) {
    if (!(o instanceof Guard<?>))
      return 1;
    if (equals (o))
      return 0;
    return new Integer(hashCode ()).compareTo (o.hashCode ());
  }
}
