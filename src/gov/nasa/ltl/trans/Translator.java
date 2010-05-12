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
package gov.nasa.ltl.trans;
//Written by Dimitra Giannakopoulou, 19 Jan 2001

import gov.nasa.ltl.graph.*;

/**
 * DOCUMENT ME!
 */
public class Translator {
  public static enum Algorithm { LTL2AUT, LTL2BUCHI };
  private static Algorithm algorithm = Algorithm.LTL2BUCHI; // by default 

  public static Algorithm getAlgorithm () {
    return algorithm;
  }

  public static void setAlgorithm (Algorithm alg) {
    algorithm = alg;
  }

  public static Graph<String> translate (String formula) {
    try {
      Formula<String> ltl = Parser.parse(formula);
      return translate(ltl);
    } catch (ParseErrorException e) {
      throw new LTLErrorException("parse error: " + e.getMessage());
    }
  }
  
  public static <PropT> Graph<PropT> translate(Formula<PropT> formula) {
    Pool pool = new Pool ();
    Node<PropT> init = Node.createInitial (formula, pool);
    Automaton<PropT> a = new Automaton<PropT> (pool);
    State<PropT>[] states = init.expand (a).structForRuntAnalysis ();
    return a.SMoutput (states);
  }
}
