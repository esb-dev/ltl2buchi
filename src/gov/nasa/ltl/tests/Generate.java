package gov.nasa.ltl.tests;

import gov.nasa.ltl.graph.Graph;
import gov.nasa.ltl.graphio.Writer;

public class Generate {
  public static void main (String[] args) {
    Graph<String> g = gov.nasa.ltl.graph.Generate.generate(5);
    Writer<String> w = Writer.getWriter (Writer.Format.FSP, System.out);
    w.write (g);
  }
}
