/**
 * 
 */
package gov.nasa.ltl.graph;

import java.util.ArrayList;

/**
 * Complete Guard implementation, except for methods to add
 * any data.
 * @author estar
 *
 */
public abstract class ListGuard<PropT> extends AbstractGuard<PropT> {
  protected ArrayList<PropT> atoms = new ArrayList<PropT> ();
  protected ArrayList<Boolean> neg = new ArrayList<Boolean> (),
                         trueLit = new ArrayList<Boolean> ();
  
  @Override
  public PropT get (int index) {
    return atoms.get (index);
  }
  
  @Override
  public boolean getNeg (int index) {
    return neg.get (index);
  }
  
  @Override
  public boolean getTrue (int index) {
    return trueLit.get (index);
  }

  @Override
  public int size () {
    return atoms.size ();
  }
  
  void append(PropT atom, boolean negated, boolean truth) {
    atoms.add (atom);
    neg.add (negated);
    trueLit.add (truth);
  }
}
