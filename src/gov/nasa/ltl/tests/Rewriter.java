/**
 * 
 */
package gov.nasa.ltl.tests;

import gov.nasa.ltl.trans.Formula;
import gov.nasa.ltl.trans.ParseErrorException;
import gov.nasa.ltl.trans.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Rewrite formulae given as parameters, using the
 * {@link gov.nasa.ltl.trans.Rewriter} class. If no
 * parameters are given, read formulae from standard
 * input and rewrite them.
 * 
 * Moved to a separate class from {@link gov.nasa.ltl.trans.Rewriter}.
 * @author Ewgenij Starostin
 *
 */
public class Rewriter {
  private static int osize = 0, rsize = 0;
  
  public static void main (String[] args) {
      if (args.length != 0)
        readArgs (args);
      else
        readStream (System.in);
  }
  
  private static void readArgs (String[] args) {
    for (String f: args)
      rewrite (f);
  }
  
  private static void readStream (InputStream in) {
    BufferedReader r = new BufferedReader (new InputStreamReader (in));
    String line = "";
    while (line != null) {
      if (!line.equals (""))
        rewrite (line);
      try {
        line = r.readLine ();
      } catch (IOException e) {
        System.err.println ("Input error " + e);
        System.exit (1);
      }
    }
  }
  
  private static void rewrite (String formula) {
    Formula<String> f;
    try {
      f = Parser.parse (formula);
    } catch (ParseErrorException e) {
      System.err.println ("Parse error on " + formula + ":" + e);
      return;
    }
    osize += f.size ();
    f = new gov.nasa.ltl.trans.Rewriter<String> (f).rewrite();
    rsize += f.size ();
    System.err.println(((rsize * 100) / osize) + "% (" + osize + 
                       " => " + rsize + ")");
  }
}
