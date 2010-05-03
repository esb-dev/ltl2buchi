/**
 * 
 */
package gov.nasa.ltl.trans;


import gov.nasa.ltl.graph.ListGuard;

import java.util.Collection;
import java.util.List;

/**
 * Abstraction for a general transition guard.
 * We assume a transition guard is a conjunction
 * of literals and leave the type of atoms variable.
 * So we treat a guard formula as list of tuples
 * &lt;atom,neg,t&gt;. Non-negated atoms have a
 * non-null atom entry, neg false, t false, negated
 * atoms have non-null atom, neg true, t false and
 * 'TRUE' literals have null atom, neg false, t true.
 * These entries can be obtained via the {@link #get(int)},
 * {@link #getNeg(int)} and {@link #getTrue(int)}
 * methods with the usual {@link List} semantics.
 * @author estar
 */
public class Guard<PropT> extends ListGuard<PropT> {
  public Guard(Collection<Formula<PropT>> formulae) {
    for (Formula<PropT> f: formulae) {
      switch(f.getContent ()) {
      case 'p':
        atoms.add (f.getName ());
        neg.add (false);
        trueLit.add (false);
        break;
      case 'N':
        atoms.add (f.getSub1 ().getName ());
        neg.add (true);
        trueLit.add (false);
        break;
      case 't':
        atoms.add (null);
        neg.add (false);
        trueLit.add (true);
        break;
      default:
        /* TODO: This should not happen, but currently
         * there is no good way to exclude the possibility.
         * A data type to represent literals would help
         * here, but this would require a lot of changes in
         * other places.
         */
        throw new RuntimeException ("Bad literal: " + f);
      }
    }
  }
}