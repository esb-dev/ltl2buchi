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
public class Graph<PropT> {
	private List<Node<PropT>> nodes;

	private Node<PropT> init;

	private Attributes attributes;

	public Graph(Attributes a) {
		init(a);
	}

	public Graph() {
		init(null);
	}

	public synchronized void setAttributes(Attributes a) {
		attributes = new Attributes(a);
	}

	/**
     * @return the attributes
     */
    public Attributes getAttributes () {
        return attributes;
    }

    public synchronized void setBooleanAttribute(String name, boolean value) {
		attributes.setBoolean(name, value);
	}

	public boolean getBooleanAttribute(String name) {
		return attributes.getBoolean(name);
	}

	public int getEdgeCount() {
		int count = 0;
		LinkedList<Node<PropT>> l = new LinkedList<Node<PropT>>(nodes);
		
		for (Node<PropT> n: l) {
			count += n.getOutgoingEdgeCount();
		}

		return count;
	}

	public synchronized void setInit(Node<PropT> n) {
		if (nodes.contains(n)) {
			init = n;
			number();
		}
	}

	public Node<PropT> getInit() {
		return init;
	}

	public synchronized void setIntAttribute(String name, int value) {
		attributes.setInt(name, value);
	}

	public int getIntAttribute(String name) {
		return attributes.getInt(name);
	}

	public Node<PropT> getNode(int id) {
		for (Node<PropT> n: nodes) {
			if (n.getId() == id) {
				return n;
			}
		}

		return null;
	}

	public int getNodeCount() {
		return nodes.size();
	}

	public List<Node<PropT>> getNodes() {
		return new LinkedList<Node<PropT>>(nodes);
	}

	public synchronized void setStringAttribute(String name, String value) {
		attributes.setString(name, value);
	}

	public String getStringAttribute(String name) {
		return attributes.getString(name);
	}

	public synchronized void dfs(Visitor<PropT> v) {
		if (init == null) {
			return;
		}

		forAllNodes(new EmptyVisitor<PropT>() {
			public void visitNode(Node<PropT> n) {
				n.setBooleanAttribute("_reached", false);
			}
		});

		dfs(init, v);

		forAllNodes(new EmptyVisitor<PropT>() {
			public void visitNode(Node<PropT> n) {
				n.setBooleanAttribute("_reached", false);
			}
		});
	}

	public synchronized void forAll(Visitor<PropT> v) {
	    LinkedList<Node<PropT>> l = new LinkedList<Node<PropT>>(nodes);
		for (Node<PropT> n: l) {
			v.visitNode(n);
			n.forAllEdges(v);
		}
	}

	public synchronized void forAllEdges(Visitor<PropT> v) {
	    LinkedList<Node<PropT>> l = new LinkedList<Node<PropT>>(nodes);
		for (Node<PropT> n: l) {
			n.forAllEdges(v);
		}
	}

	public synchronized void forAllNodes(Visitor<PropT> v) {
	    LinkedList<Node<PropT>> l = new LinkedList<Node<PropT>>(nodes);
		for (Node<PropT> n: l) {
			v.visitNode(n);
		}
	}

	synchronized void addNode(Node<PropT> n) {
		nodes.add(n);

		if (init == null) {
			init = n;
		}

		number();
	}

	synchronized void removeNode(Node<PropT> n) {
		nodes.remove(n);

		if (init == n) {
			if (nodes.size() != 0) {
				init = nodes.get(0);
			} else {
				init = null;
			}
		}

		number();
	}

	private void init(Attributes a) {
		if (a == null) {
			attributes = new Attributes();
		} else {
			attributes = a;
		}

		nodes = new LinkedList<Node<PropT>>();
		init = null;
	}

	public synchronized void number() {
		int cnt;

		if (init != null) {
			init.setId(0);
			cnt = 1;
		} else {
			cnt = 0;
		}

		for (Node<PropT> n: nodes) {
			if (n != init) {
				n.setId(cnt++);
			}
		}
	}

	private synchronized void dfs(Node<PropT> n, Visitor<PropT> v) {
		final Visitor<PropT> visitor = v;

		if (n.getBooleanAttribute("_reached")) {
			return;
		}

		n.setBooleanAttribute("_reached", true);

		v.visitNode(n);

		n.forAllEdges(new EmptyVisitor<PropT>() {
			public void visitEdge(Edge<PropT> e) {
				dfs(e.getNext(), visitor);
			}
		});
	}
}
