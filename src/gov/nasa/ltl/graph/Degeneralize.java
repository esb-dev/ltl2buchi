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


/**
 * DOCUMENT ME!
 */
public class Degeneralize {
  public static <PropT> Graph<PropT> degeneralize (Graph<PropT> g) {
    int    nsets = g.getIntAttribute("nsets");
    String type = g.getStringAttribute("type");

    if (type.equals("gba")) {
      String ac = g.getStringAttribute("ac");

      if (ac.equals("nodes")) {
        if (nsets == 1) {
          accept(g);
        } else {
          Label.label(g);

          Graph<String> d = Generate.generate(nsets);


          //    d.save(Graph.FSP_FORMAT);
          g = SynchronousProduct.product(g, d);
        }
      } else if (ac.equals("edges")) {
        Graph<String> d = Generate.generate(nsets);
        g = SynchronousProduct.product(g, d);
      }
    } else if (!type.equals("ba")) {
      throw new RuntimeException("invalid graph type: " + type);
    }

    return g;
  }

  private static <PropT> void accept (Graph<PropT> g) {
    g.setBooleanAttribute("nsets", false);

    g.forAllNodes(new EmptyVisitor<PropT>() {
      public void visitNode (Node<PropT> n) {
        if (n.getBooleanAttribute("acc0")) {
          n.setBooleanAttribute("accepting", true);
          n.setBooleanAttribute("acc0", false);
        }
      }
    });
  }
}
