/**
 * 
 */
package gov.nasa.ltl.graphio;

import java.io.PrintStream;
import java.util.Map;

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
class SMWriter<PropT> extends Writer<PropT> {
  protected PrintStream out;
  
  SMWriter(PrintStream s) {
    out = s;
  }

  /* (non-Javadoc)
   * @see gov.nasa.ltl.graphio.Writer#write(gov.nasa.ltl.graph.Graph)
   */
  @Override
  public void write (Graph<PropT> g) {
    boolean first = true;
    Map<Object, String> attr = g.getAttributes ().getAll ();
    
    out.println (g.getNodeCount ());
    for (Object key: attr.keySet ()) {
      if (first)
        first = false;
      else
        out.print (',');
      out.print (key);
      if (attr.get (key) != null && !attr.get (key).equals ("")) {
        out.print ('=');
        out.print (attr.get (key));
      }
    }
    out.println ();
    if (g.getInit () != null)
      write (g.getInit ());
    for (Node<PropT> n: g.getNodes ()) {
      if (n == g.getInit ())
        continue;
      write (n);
    }
  }

  /* (non-Javadoc)
   * @see gov.nasa.ltl.graphio.Writer#write(gov.nasa.ltl.graph.Node)
   */
  @Override
  public void write (Node<PropT> n) {
    Map<Object, String> attr = n.getAttributes ().getAll ();
    boolean first = true;
    
    out.print ("  " + n.getOutgoingEdgeCount () + "  ");
    for (Object key: attr.keySet ()) {
      if (key.equals ("_id"))
        continue;
      if (first)
        first = false;
      else
        out.print (',');
      out.print (key);
      if (attr.get (key) != null && !attr.get (key).equals ("")) {
        out.print ('=');
        out.print (attr.get (key));
      }
    }
    out.println ();
    for (Edge<PropT> e: n.getOutgoingEdges ())
      write (e);
  }

  /* (non-Javadoc)
   * @see gov.nasa.ltl.graphio.Writer#write(gov.nasa.ltl.graph.Edge)
   */
  @Override
  public void write (Edge<PropT> e) {
    Map<Object, String> attr = e.getAttributes ().getAll ();
    boolean first = true;

    out.println ("    " + e.getNext ().getId ());
    out.print ("    ");
    write (e.getGuard ());
    out.println ("    " + e.getAction ());
    out.print ("    ");
    for (Object key: attr.keySet ()) {
      if (first)
        first = false;
      else
        out.print (',');
      out.print (key);
      if (attr.get (key) != null && !attr.get (key).equals ("")) {
        out.print ('=');
        out.print (attr.get (key));
      }
    }
    out.println ();
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
      out.print ("TRUE");
      return;
    }
    for (Literal<PropT> l: g) {
      if (first)
        first = false;
      else
        out.print ('&');
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
