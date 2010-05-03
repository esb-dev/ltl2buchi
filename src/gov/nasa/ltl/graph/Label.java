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
package gov.nasa.ltl.graph;

import gov.nasa.ltl.graphio.Reader;

import java.io.IOException;


/**
 * DOCUMENT ME!
 */
public class Label {
  public static <PropT> Graph<PropT> label (Graph<PropT> g) {
    String type = g.getStringAttribute("type");
    String ac = g.getStringAttribute("ac");

    if (type.equals("gba")) {
      if (ac.equals("nodes")) {
        final int nsets = g.getIntAttribute("nsets");

        g.forAllNodes(new EmptyVisitor<PropT>() {
          public void visitNode (Node<PropT> n) {
            n.forAllEdges(new EmptyVisitor<PropT>() {
              public void visitEdge (Edge<PropT> e) {
                Node<PropT> n1 = e.getSource();

                for (int i = 0; i < nsets; i++) {
                  if (n1.getBooleanAttribute("acc" + i)) {
                    e.setBooleanAttribute("acc" + i, true);
                  }
                }
              }
            });

            for (int i = 0; i < nsets; i++) {
              n.setBooleanAttribute("acc" + i, false);
            }
          }
        });
      }

      g.setStringAttribute("ac", "edges");
    } else {
      throw new RuntimeException("invalid graph type: " + type);
    }

    return g;
  }

  public static void main (String[] args) {
    try {
      Graph<String> g = Reader.read(args[0]);
      label(g);
      g.save();
    } catch (IOException e) {
      System.err.println("Can't load file: " + args[0]);
      System.exit(1);
    }
  }
}
