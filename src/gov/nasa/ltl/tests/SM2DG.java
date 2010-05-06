//
// Copyright (C) 2006 United States Government as represented by the
// Administrator of the National Aeronautics and Space Administration
// (NASA).  All Rights Reserved.
// 
// This software is distributed under the NASA Open Source Agreement
// (NOSA), version 1.3.  The NOSA has been approved by the Open Source
// Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
// directory tree for the complete NOSA document.
// 
// THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
// KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
// LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
// SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
// A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
// THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
// DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//
package gov.nasa.ltl.tests;

import gov.nasa.ltl.graph.Graph;
import gov.nasa.ltl.graphio.Reader;
import gov.nasa.ltl.graphio.Writer;

import java.io.IOException;
import java.io.PrintStream;


/**
 * DOCUMENT ME!
 */
public class SM2DG {
  public static void main (String[] args) {
    Writer<String> w = Writer.getWriter (Writer.Format.FSP, System.out);

    try {
      Graph<String> g = null;

      switch (args.length) {
      case 0:
        g = Reader.read("out.sm");
        w.write (g);
        break;
      case 1:
        g = Reader.read(args[0]);
        w.write (g);
        break;
      case 2:
        g = Reader.read(args[0]);
        w = Writer.getWriter (Writer.Format.FSP,
            new PrintStream (args[1]));
        w.write (g);
        break;
      default:
        System.err.println("usage:\ngov.nasa.ltl.tests.SM2DG [<infile> [<outfile>]]\n\n");
        System.exit(1);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
