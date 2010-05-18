package gov.nasa.ltl.tests;

import gov.nasa.ltl.graph.Graph;
import gov.nasa.ltl.graphio.Reader;
import gov.nasa.ltl.graphio.Writer;

import java.io.IOException;

public class SynchronousProduct {
  public static void main (String[] args) {
    Graph<String> g0;
    Graph<String> g1;

    try {
      g0 = Reader.read(args[0]);
      g1 = Reader.read(args[1]);
    } catch (IOException e) {
      System.err.println("Can't load automata");
      System.exit(1);

      return;
    }

    Graph<String> g = gov.nasa.ltl.graph.SynchronousProduct.product(g0, g1);
    Writer<String> w = Writer.getWriter (Writer.Format.SM, System.out);
    w.write (g);
  }
}
