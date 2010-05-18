package gov.nasa.ltl.tests;

import gov.nasa.ltl.graph.Graph;
import gov.nasa.ltl.graph.Node;
import gov.nasa.ltl.graphio.Reader;
import gov.nasa.ltl.graphio.Writer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class SCC {
  public static void help () {
    System.err.println("usage:");
    System.err.println("\tgov.nasa.ltl.tests.SCC [<outfile>]");
    System.exit(1);
  }

  public static void main (String[] args) {
    String outname = null;
    Graph<String> g;
    Writer<String> w = null;

    switch (args.length) {
    case 0:
      break;
    case 1:
      outname = args[0];
    default:
      help ();
    }
    try {
      g = Reader.read("out.sm");
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    List<List<Node<String>>>  scc = gov.nasa.ltl.graph.SCC.scc(g);
    for (List<Node<String>> l: scc) {
      System.out.println("component:");
      for (Node<String> n: l) {
        System.out.println("  " + n.getStringAttribute("label"));
      }
      System.out.println();
    }
    if (outname == null)
      w = Writer.getWriter (Writer.Format.SM, System.out);
    else
      try {
        w = Writer.getWriter (Writer.Format.SM,
            new PrintStream (outname));
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        System.exit (1);
      }
    w.write (g);
  }

  public static <PropT> void print (List<List<Node<PropT>>> sccs) {
    System.out.println("Strongly connected components:");

    int cnt = 0;

    for (List<Node<PropT>> scc: sccs) {
      System.out.println("\tSCC #" + (cnt++));

      for (Node<PropT> n: scc) {
        System.out.println("\t\t" + n.getId() + " - " + 
                           n.getStringAttribute("label"));
      }
    }
  }
}
