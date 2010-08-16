/**
 * 
 */
package gov.nasa.ltl.graphio;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import gov.nasa.ltl.graph.Guard;
import gov.nasa.ltl.graph.Literal;
import gov.nasa.ltl.graph.Edge;
import gov.nasa.ltl.graph.Graph;
import gov.nasa.ltl.graph.Node;
import gov.nasa.ltl.trans.State;
import gov.nasa.ltl.trans.Transition;

/**
 * @author Ewgenij Starostin
 * @param <PropT>
 *
 */
public abstract class Writer<PropT> {
  public static enum Format {
    SM, FSP, XML, SPIN
  }
  
  /**
   * Print a graph in some data format.
   * @param g graph
   */
  public abstract void write (Graph<PropT> g);
  /**
   * Print a vertex in some data format.
   * @param n vertex
   */
  public abstract void write (Node<PropT> n);
  /**
   * Print an edge in some data format.
   * @param e edge
   */
  public abstract void write (Edge<PropT> e);
  /**
   * Print a graph in some data format.
   * @param states set of vertices making up the graph
   */
  public abstract void write (State<PropT>[] states);
  /**
   * Print a vertex in some data format.
   * @param s vertex
   */
  public abstract void write (State<PropT> s);
  /**
   * Print an edge in some data format.
   * @param t edge
   */
  public abstract void write (Transition<PropT> t);
  /**
   * Print an edge label in some data format.
   * @param g label
   */
  public abstract void write (Guard<PropT> g);
  /**
   * Print a literal in some data format.
   * @param l literal
   */
  public abstract void write (Literal<PropT> l);
  
  /**
   * Obtain an instance of an Writer subclass for a given
   * output format.
   * @param <PropT>
   * @param f format to be used
   * @return
   */
  public static final <PropT> Writer<PropT> getWriter (Format f, PrintStream out) {
    switch (f) {
    case SM:
      return new SMWriter<PropT> (out);
    case FSP:
      return new FSPWriter<PropT> (out);
    case XML:
      return new XMLWriter<PropT> (out);
    case SPIN:
      return new SpinWriter<PropT> (out);
    }
    return null;
  }
  
  /**
   * Helper function to format a guard in SM format, because this is
   * needed in several places.
   * @param <PropT>
   * @param g guard
   * @return string of PropT atoms, !, & and TRUE, or "-"
   */
  public static <PropT> String formatSMGuard (Guard<PropT> g) {
    ByteArrayOutputStream s = new ByteArrayOutputStream ();
    PrintStream p = new PrintStream (s);
    new SMWriter<PropT> (p).write (g);
    return s.toString ();
  }
}
