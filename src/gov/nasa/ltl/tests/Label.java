package gov.nasa.ltl.tests;

import gov.nasa.ltl.graph.Graph;
import gov.nasa.ltl.graphio.Reader;
import gov.nasa.ltl.graphio.Writer;

import java.io.IOException;

public class Label {
  public static void main (String[] args) {
    if (args.length < 1) {
      System.err.println ("usage: gov.nasa.ltl.tests.Label <filename>");
      System.exit (1);
    }
    Graph<String> g = null;
    try {
      g = Reader.read(args[0]);
    } catch (IOException e) {
      System.err.println("Can't load file: " + args[0]);
      System.exit(1);
    }
    gov.nasa.ltl.graph.Label.label(g);
    Writer<String> w = Writer.getWriter (Writer.Format.SM, System.out);
    w.write (g);
  }
}
