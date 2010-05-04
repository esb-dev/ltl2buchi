/**
 * 
 */
package gov.nasa.ltl.trans;


import gov.nasa.ltl.graph.Literal;

import java.util.Collection;

/**
 * 
 * @author estar
 */
public class Guard<PropT> extends gov.nasa.ltl.graph.Guard<PropT> {
  private static final long serialVersionUID = -1099850840173426153L;

  public Guard(Collection<Formula<PropT>> literals) {
    for (Formula<PropT> f: literals) {
      switch(f.getContent ()) {
      case 'p':
        add (new Literal<PropT> (f.getName (), false, false));
        break;
      case 'N':
        assert f.getSub1 ().getContent () == 'p';
        add (new Literal<PropT> (f.getSub1 ().getName (), true, false));
        break;
      case 't':
        add (new Literal<PropT> (null, false, true));
        break;
      default:
        /* TODO: This should not happen, but currently
         * there is no good way to exclude the possibility.
         * A data type to represent literals would help
         * here, but this would require a lot of changes in
         * other places.
         */
        assert false : "Bad literal: " + f;
      }
    }
  }
}