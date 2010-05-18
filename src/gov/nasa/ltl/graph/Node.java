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

import java.util.LinkedList;
import java.util.List;

/**
 * DOCUMENT ME!
 */
public class Node<PropT> {
	private Graph<PropT> graph;

	private List<Edge<PropT>> outgoingEdges;

	private List<Edge<PropT>> incomingEdges;

	private Attributes attributes;

	public Node(Graph<PropT> g, Attributes a) {
		init(g, a);
	}

	public Node(Graph<PropT> g) {
		init(g, null);
	}

	public Node(Node<PropT> n) {
		init(n.graph, new Attributes(n.attributes));

		for (Edge<PropT> e: n.outgoingEdges) {
			new Edge<PropT>(this, e);
		}

		for (Edge<PropT> e: n.incomingEdges) {
			new Edge<PropT>(e, this);
		}
	}

	public synchronized void setAttributes(Attributes a) {
		int id = getId();
		attributes = new Attributes(a);
		setId(id);
	}

	public Attributes getAttributes() {
		return attributes;
	}

	public synchronized void setBooleanAttribute(String name, boolean value) {
		if (name.equals("_id")) {
			return;
		}

		attributes.setBoolean(name, value);
	}

	public boolean getBooleanAttribute(String name) {
		return attributes.getBoolean(name);
	}

	public Graph<PropT> getGraph() {
		return graph;
	}

	public synchronized int getId() {
		return attributes.getInt("_id");
	}

	public int getIncomingEdgeCount() {
		return outgoingEdges.size();
	}

	public List<Edge<PropT>> getIncomingEdges() {
		return new LinkedList<Edge<PropT>>(incomingEdges);
	}

	public synchronized void setIntAttribute(String name, int value) {
		if (name.equals("_id")) {
			return;
		}

		attributes.setInt(name, value);
	}

	public int getIntAttribute(String name) {
		return attributes.getInt(name);
	}

	public int getOutgoingEdgeCount() {
		return outgoingEdges.size();
	}

	public List<Edge<PropT>> getOutgoingEdges() {
		return new LinkedList<Edge<PropT>>(outgoingEdges);
	}

	public synchronized void setStringAttribute(String name, String value) {
		if (name.equals("_id")) {
			return;
		}

		attributes.setString(name, value);
	}

	public String getStringAttribute(String name) {
		return attributes.getString(name);
	}

	public synchronized void forAllEdges(Visitor<PropT> v) {
	    LinkedList<Edge<PropT>> l = new LinkedList<Edge<PropT>>(outgoingEdges);
		for (Edge<PropT> e: l) {
			v.visitEdge(e);
		}
	}

	public synchronized void remove() {
        LinkedList<Edge<PropT>> l = new LinkedList<Edge<PropT>>(outgoingEdges);
		for (Edge<PropT> e: l) {
			e.remove();
		}
        LinkedList<Edge<PropT>> k = new LinkedList<Edge<PropT>>(incomingEdges);
		for (Edge<PropT> e: k) {
			e.remove();
		}

		graph.removeNode(this);
	}

	synchronized void setId(int id) {
		attributes.setInt("_id", id);
	}

	synchronized void addIncomingEdge(Edge<PropT> e) {
		incomingEdges.add(e);
	}

	synchronized void addOutgoingEdge(Edge<PropT> e) {
		outgoingEdges.add(e);
	}

	synchronized void removeIncomingEdge(Edge<PropT> e) {
		incomingEdges.remove(e);
	}

	synchronized void removeOutgoingEdge(Edge<PropT> e) {
		outgoingEdges.remove(e);
	}

	private void init(Graph<PropT> g, Attributes a) {
		graph = g;

		if (a == null) {
			attributes = new Attributes();
		} else {
			attributes = a;
		}

		incomingEdges = new LinkedList<Edge<PropT>>();
		outgoingEdges = new LinkedList<Edge<PropT>>();

		graph.addNode(this);
	}
}
