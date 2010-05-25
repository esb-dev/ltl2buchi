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
//Written by Dimitra and Flavio (2001)
//Some modifications by: Roby Joehanes

import gov.nasa.ltl.graph.*;
import gov.nasa.ltl.graphio.Writer;

import java.io.*;

/**
 * DOCUMENT ME!
 */
public class LTL2Buchi {
	public static boolean debug = false; // TODO: make this private again... eventually

	public static void main(String[] args) {
		String ltl = null;
		boolean rewrite = true;
		boolean bisim = true;
		boolean fairSim = true;
		boolean file_provided = false;
		Writer.Format format = Writer.Format.FSP;
		debug = true;

		System.out.println("\nAuthors Dimitra Giannakopoulou & Flavio Lerda, \n(c) 2001,2003 NASA Ames Research Center\n");

		Translator.setAlgorithm(Translator.Algorithm.LTL2BUCHI);

		if (args.length != 0) {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("usage"))
					usage_warning();
				if (args[i].equals("-a")) {
					i++;

					if (i < args.length) {
						if (args[i].equals("ltl2buchi")) {
							Translator.setAlgorithm(Translator.Algorithm.LTL2BUCHI);
						} else if (args[i].equals("ltl2aut")) {
							Translator.setAlgorithm(Translator.Algorithm.LTL2AUT);
						} else {
							usage_warning();

							return;
						}
					} else {
						usage_warning();

						return;
					}
				} else if (args[i].equals("-norw")) {
					rewrite = false;
				} else if (args[i].equals("-nobisim")) {
					bisim = false;
				} else if (args[i].equals("-nofsim")) {
					fairSim = false;
				} else if (args[i].equals("-nodebug")) {
					debug = false;
				} else if (args[i].equals("-o")) {
					i++;

					if (i < args.length) {
						if (args[i].equals("fsp"))
							format = Writer.Format.FSP;
						else if (args[i].equals("promela"))
							format = Writer.Format.SPIN;
						else if (args[i].equals("xml"))
							format = Writer.Format.XML;
					}

				} else if (args[i].equals("-f")) {
					i++;

					if (i < args.length) {
						ltl = args[i];

						if (ltl.endsWith(".ltl")) {
							ltl = loadLTL(ltl);
							file_provided = true;
						} else if (ltl.equals("-")) {
						} else {
							usage_warning();

							return;
						}
					} else {
						usage_warning();

						return;
					}
				} else {
					usage_warning();

					return;
				}
			}
		}

		if (!file_provided) {
			ltl = readLTL();
		}

		try {
			Graph<String> g = translate(ltl, rewrite, bisim, fairSim);
			Writer<String> w = Writer.getWriter (format, System.out);
			w.write (g);
			System.out.println("\n***********************\n");
		} catch (ParseErrorException ex) {
			System.out.println("Error: " + ex);
		}
	}

	public static void reset_all_static() {
		Formula.resetStatic();
	}

	public static Graph<String> translate(String formula, boolean rewrite,
			boolean bisim, boolean fair_sim) throws ParseErrorException {
		//	System.out.println("Translating formula: " + formula);
		// System.out.println();
		return translate(Parser.parse (formula), rewrite, bisim, fair_sim);
	}

	/**
	 * Print the number of vertices and edges in a graph, and what was
	 * done to obtain it.
	 * @param <PropT>
	 * @param gba
	 * @param op
	 */
	private static <PropT> void printStats(Graph<PropT> gba, String op) {
	  if (!debug)
	    return;
      System.out.println("\n***********************");
      System.out.println("\n" + op);
      System.out.println("\t" + gba.getNodeCount() + " states "
              + gba.getEdgeCount() + " transitions");
	}
	
	public static <PropT> Graph<PropT> translate(Formula<PropT> formula,
	    boolean rewrite, boolean bisim, boolean fair_sim) {
        if (rewrite) {
          formula = new Rewriter<PropT> (formula).rewrite();
          if (debug)
            System.out.println("Rewritten as      : " + formula + "\n");
        }
		Graph<PropT> gba = Translator.translate(formula);
		printStats(gba, "Generalized buchi automaton generated");
		gba = SuperSetReduction.reduce(gba);
		printStats(gba, "Superset reduction");
		Graph<PropT> ba = Degeneralize.degeneralize(gba);
		printStats(ba, "Degeneralized buchi automaton generated");
		ba = SCCReduction.reduce(ba);
	    printStats(ba, "Strongly connected component reduction");
		if (bisim) {
			ba = Simplify.simplify(ba);
		    printStats(ba, "Bisimulation applied");
		}
		if (fair_sim) {
			ba = SFSReduction.reduce(ba);
		    printStats(ba, "Fair simulation applied");
		}
		if (debug)
		  System.out.println("***********************\n");
		reset_all_static();
		return ba;
	}

	public static Graph<String> translate(String formula) throws ParseErrorException {
		// To work with Bandera and JPF
		return translate(formula, true, true, true);
	}
	
	public static <PropT> Graph<PropT> translate(Formula<PropT> formula) {
	    return translate(formula, true, true, true);
	}

	public static Graph<String> translate(File file) throws ParseErrorException {
		String formula = "";

		try {
			LineNumberReader f = new LineNumberReader(new FileReader(file));
			formula = f.readLine().trim();
			f.close();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}

		return translate(formula, true, true, true);
	}

	public static void usage_warning() {
		System.out.println("\n*******  USAGE *******");
		System.out.println("java gov.nasa.ltl.trans.LTL2Buchi <options>");
		System.out.println("\toptions can be (in any order):");
		System.out
				.println("\t\t \"-f <filename.ltl>\" (read formula from file)");
		System.out
				.println("\t\t \"-a [ltl2buchi|ltl2aut]\" (set algorithm to be used)");
		System.out.println("\t\t \"-norw\" (no rewriting)");
		System.out.println("\t\t \"-nobisim\" (no bisimulation reduction)");
		System.out.println("\t\t \"-nofsim\" (no fair simulation reduction)");
		System.out
				.println("\t\t \"-o [fsp|promela|xml>\" (format of output; default is fsp)");

		return;
	}

	private static String loadLTL(String fname) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(fname));
			String line = in.readLine();
      in.close();
			return line;
		} catch (FileNotFoundException e) {
			throw new LTLErrorException("Can't load LTL formula: " + fname);
		} catch (IOException e) {
			throw new LTLErrorException("Error read on LTL formula: " + fname);
		}
	}

	private static String readLTL() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in));

			System.out.print("\nInsert LTL formula: ");

			return in.readLine();
		} catch (IOException e) {
			throw new LTLErrorException("Invalid LTL formula");
		}
	}
}
