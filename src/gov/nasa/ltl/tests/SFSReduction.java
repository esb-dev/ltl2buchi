package gov.nasa.ltl.tests;

import gov.nasa.ltl.graph.Graph;
import gov.nasa.ltl.graphio.Reader;
import gov.nasa.ltl.graphio.Writer;

import java.io.IOException;

public class SFSReduction {
  public static void main (String[] args) {
    if (args.length > 1) {
      System.err.println("usage:");
      System.err.println("\tjava gov.nasa.ltl.tests.SFSReduction [<filename>]");
      System.exit (1);
    }

    Graph<String> g = null;

    try {
      if (args.length == 0) {
        g = Reader.read();
      } else {
        g = Reader.read(args[0]);
      }
    } catch (IOException e) {
      System.out.println("Can't load the graph.");

      return;
    }

    Graph<String> reduced = gov.nasa.ltl.graph.SFSReduction.reduce(g);
    Writer<String> w = Writer.getWriter (Writer.Format.SM, System.out);
    w.write (reduced);
  }
}
