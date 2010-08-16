/**
 * 
 */
package gov.nasa.ltl.graphio;

import java.io.PrintStream;

import gov.nasa.ltl.graph.Edge;
import gov.nasa.ltl.graph.Graph;
import gov.nasa.ltl.graph.Guard;
import gov.nasa.ltl.graph.Literal;
import gov.nasa.ltl.graph.Node;
import gov.nasa.ltl.trans.State;
import gov.nasa.ltl.trans.Transition;

/**
 * @author Ewgenij Starostin
 *
 */
class SpinWriter<PropT> extends Writer<PropT> {
  protected PrintStream out;
  
  SpinWriter(PrintStream s) {
    out = s;
  }

  /* (non-Javadoc)
   * @see gov.nasa.ltl.graphio.Writer#write(gov.nasa.ltl.graph.Graph)
   */
  @Override
  public void write (Graph<PropT> g) {
    if (g.getInit () == null) {
      out.println ("Empty");
      return;
    }
    out.println ("never {");
    write (g.getInit ());
    for (Node<PropT> n: g.getNodes ()) {
      if (n == g.getInit ())
        continue;
      write (n);
      out.println ();
    }
    out.println ("}");
  }

  /* (non-Javadoc)
   * @see gov.nasa.ltl.graphio.Writer#write(gov.nasa.ltl.graph.Node)
   */
  @Override
  public void write (Node<PropT> n) {
    if (n.getBooleanAttribute ("accepting"))
      out.print ("accept_");
    out.println ("S" + n.getId () + ":");
    out.println ("     if");
    for (Edge<PropT> e: n.getOutgoingEdges ()) {
      out.print ("     :: ");
      write (e);
    }
    out.println ("     fi;");
  }

  /* (non-Javadoc)
   * @see gov.nasa.ltl.graphio.Writer#write(gov.nasa.ltl.graph.Edge)
   */
  @Override
  public void write (Edge<PropT> e) {
    int nsets = e.getSource ().getGraph ().getIntAttribute ("nsets");
    
    out.print ('(');
    write (e.getGuard ());
    out.print (") ");
    if (nsets == 0)
      if (e.getBooleanAttribute ("accepting"))
        out.print ('@');
      else
        /* nothing */;
    else {
      boolean first = true;
      
      for (int i = 0; i < nsets; i++) {
        if (!e.getBooleanAttribute ("acc" + i))
          continue;
        if (first) {
          out.print ('{');
          first = false;
        }
        else
          out.print (',');
        out.print (i);
      }
      if (!first)
        out.print ('}');
    }
    out.print ("-> goto ");
    if (e.getNext ().getBooleanAttribute ("accepting"))
      out.print ("accept_");
    out.println ("S" + e.getNext ().getId ());
  }

  /**
   * Not implemented.
   */
  @Override
  public void write (State<PropT>[] states) {
    throw new RuntimeException ("Not implemented.");
  }

  /**
   * Not implemented.
   */
  @Override
  public void write (State<PropT> s) {
    throw new RuntimeException ("Not implemented.");
  }

  /**
   * Not implemented.
   */
  @Override
  public void write (Transition<PropT> t) {
    throw new RuntimeException ("Not implemented.");
  }

  /* (non-Javadoc)
   * @see gov.nasa.ltl.graphio.Writer#write(gov.nasa.ltl.graph.Guard)
   */
  @Override
  public void write (Guard<PropT> g) {
    boolean first = true;
    
    if (g.isTrue ()) {
      out.print ('1');
      return;
    }
    for (Literal<PropT> l: g) {
      if (first)
        first = false;
      else
        out.print (" && ");
      write (l);
    }
  }

  /* (non-Javadoc)
   * @see gov.nasa.ltl.graphio.Writer#write(gov.nasa.ltl.graph.Literal)
   */
  @Override
  public void write (Literal<PropT> l) {
    if (l.isNegated ())
      out.print ('!');
    out.print (l.getAtom ());
  }
}
