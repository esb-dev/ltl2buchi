/**
 * 
 */
package gov.nasa.ltl.graphio;

import java.io.PrintStream;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
public class XMLWriter<PropT> extends Writer<PropT> {
  protected PrintStream out;
  
  XMLWriter(PrintStream s) {
    out = s;
  }

  /* (non-Javadoc)
   * @see gov.nasa.ltl.graphio.Writer#write(gov.nasa.ltl.graph.Graph)
   */
  @Override
  public void write (Graph<PropT> g) {
    DocumentBuilder db = null;
    Document doc;
    Element graph;
    Transformer transformer = null;
    DOMSource source;
    StreamResult result;
    
    try {
      db = DocumentBuilderFactory.newInstance ().newDocumentBuilder ();
      transformer = TransformerFactory.newInstance ().newTransformer ();
    } catch (ParserConfigurationException e) {
      assert false : e;
      return;
    } catch (TransformerConfigurationException e) {
      assert false : e;
      return;
    } catch (TransformerFactoryConfigurationError e) {
      assert false : e;
      return;
    }
    doc = db.newDocument ();
    graph = doc.createElement ("graph");
    graph.setAttribute ("nodes", "" + g.getNodeCount ());
    for (Node<PropT> n: g.getNodes ()) {
      Map<Object, String> attr = n.getAttributes ().getAll ();
      Element node = doc.createElement ("node");
      node.setAttribute ("id", "" + n.getId ());
      if (n == g.getInit ()) {
        Element init = doc.createElement ("init");
        init.appendChild (doc.createTextNode ("true"));
        node.appendChild (init);
      }
      for (Object key: attr.keySet ()) {
        if (("" + key).equals ("_id"))
          continue;
        Element k = doc.createElement ("" + key);
        if (!attr.get (key).equals (""))
          k.appendChild (doc.createTextNode (attr.get (key)));
        node.appendChild (k);
      }
      for (Edge<PropT> t: n.getOutgoingEdges ()) {
        Element trans = doc.createElement ("transition");
        trans.setAttribute ("to", "" + t.getNext ().getId ());
        if (t.getGuard () != null && !t.getGuard ().isTrue ()) {
          Element guard = doc.createElement ("guard");
          guard.appendChild (
              doc.createTextNode (formatSMGuard (t.getGuard ())));
          trans.appendChild (guard);
        }
        if (t.getAction () != null && !t.getAction ().equals ("-")) {
          Element action = doc.createElement ("action");
          action.appendChild (doc.createTextNode (t.getAction ()));
          trans.appendChild (action);
        }
        node.appendChild (trans);
      }
      graph.appendChild (node);
    }
    doc.appendChild (graph);
    source = new DOMSource (doc);
    result = new StreamResult (out);
    try {
      transformer.transform (source, result);
    } catch (TransformerException e) {
      e.printStackTrace();
    }
  }

  /**
   * Not implemented.
   */
  @Override
  public void write (Node<PropT> n) {
    throw new RuntimeException ("Not implemented.");
  }

  /**
   * Not implemented.
   */
  @Override
  public void write (Edge<PropT> e) {
    throw new RuntimeException ("Not implemented.");
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

  /**
   * Not implemented.
   */
  @Override
  public void write (Guard<PropT> g) {
    throw new RuntimeException ("Not implemented.");
  }

  /**
   * Not implemented.
   */
  @Override
  public void write (Literal<PropT> l) {
    throw new RuntimeException ("Not implemented.");
  }
}
