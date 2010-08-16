/**
 * 
 */
package gov.nasa.ltl.graphio;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import gov.nasa.ltl.graph.Attributes;
import gov.nasa.ltl.graph.Edge;
import gov.nasa.ltl.graph.Graph;
import gov.nasa.ltl.graph.Literal;
import gov.nasa.ltl.graph.Node;

/**
 * @author Ewgenij Starostin
 * 
 */
public class Reader {
  private static class Guard extends gov.nasa.ltl.graph.Guard<String> {
    private static final long serialVersionUID = -4427343445274801521L;
    
    public Guard (String formula) {
      for (String token: formula.split ("&")) {
        token = token.trim ();
        if (token.equals ("TRUE"))
          /* Nothing happens. */;
        else
          if (token.substring (0, 1).equals ("!"))
            add (new Literal<String> (token.substring (1).trim (), true));
          else
            add (new Literal<String> (token.trim (), false));
      }
    }
  }
  
  @SuppressWarnings ("unchecked")
  public static Graph<String> read (BufferedReader in) throws IOException {
    int ns = readInt(in);
    Node<String>[] nodes = new Node[ns];

    Graph<String> g = new Graph<String>(readAttributes(in));

    for (int i = 0; i < ns; i++) {
        int nt = readInt(in);

        if (nodes[i] == null) {
            nodes[i] = new Node<String>(g, readAttributes(in));
        } else {
            nodes[i].setAttributes(readAttributes(in));
        }

        for (int j = 0; j < nt; j++) {
            int nxt = readInt(in);
            String gu = readString(in);
            Guard guard = new Guard (gu);
            String ac = readString(in);

            if (nodes[nxt] == null) {
                nodes[nxt] = new Node<String>(g);
            }

            new Edge<String>(nodes[i], nodes[nxt], guard, ac, readAttributes(in));
        }
    }

    g.number();

    return g;
  }

  public static Graph<String> read () throws IOException {
    return read (new BufferedReader (new InputStreamReader (System.in)));
  }

  public static Graph<String> read (String filename) throws IOException {
    return read (new BufferedReader (new FileReader (filename)));
  }

  private static Attributes readAttributes (BufferedReader in)
      throws IOException {
    String line = readLine (in);
    Attributes attr = new Attributes ();
    for (String a: line.split (",")) {
      int eq = a.indexOf ('=');
      String key, value;
      if (eq != -1) {
        key = a.substring (0, eq);
        value = a.substring (eq + 1);
      } else {
        key = a;
        value = "";
      }
      attr.setString (key, value);
    }
    return attr;
  }

  private static int readInt (BufferedReader in) throws IOException {
    return Integer.parseInt (readLine (in));
  }

  private static String readLine (BufferedReader in) throws IOException {
    String line;

    do {
      line = in.readLine ();

      int idx = line.indexOf ('#');

      if (idx != -1) {
        line = line.substring (0, idx);
      }

      line = line.trim ();
    } while (line.length () == 0);

    return line;
  }

  private static String readString (BufferedReader in) throws IOException {
    return readLine (in);
  }
}