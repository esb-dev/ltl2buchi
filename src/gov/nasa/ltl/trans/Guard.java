/**
 * 
 */
package gov.nasa.ltl.trans;


import gov.nasa.ltl.graph.Literal;
import gov.nasa.ltl.trans.Formula.Content;

import java.util.Collection;

/**
 * 
 * @author Ewgenij Starostin
 */
public class Guard<PropT> extends gov.nasa.ltl.graph.Guard<PropT> {
  private static final long serialVersionUID = -1099850840173426153L;

  public Guard(Collection<Formula<PropT>> literals) {
    for (Formula<PropT> f: literals) {
      switch(f.getContent ()) {
      case PROPOSITION:
        add (new Literal<PropT> (f.getName (), false));
        break;
      case NOT:
        assert f.getSub1 ().getContent () == Content.PROPOSITION;
        add (new Literal<PropT> (f.getSub1 ().getName (), true));
        break;
      case TRUE:
        // Nothing happens.
        break;
      default:
        assert false : "Not a literal: " + f;
      }
    }
  }
}