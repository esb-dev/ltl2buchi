package gov.nasa.ltl.tests;

import gov.nasa.ltl.graph.Graph;
import gov.nasa.ltl.graphio.Reader;
import gov.nasa.ltl.graphio.Writer;

import java.io.IOException;

public class SuperSetReduction {
  public static void main (String[] args) {
    if (args.length > 1) {
      System.out.println("usage:");
      System.out.println(
            "\tjava gov.nasa.ltl.tests.SuperSetReduction [<filename>]");
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

    g = gov.nasa.ltl.graph.SuperSetReduction.reduce(g);
    Writer<String> w = Writer.getWriter (Writer.Format.SM, System.out);
    w.write (g);
  }
}
