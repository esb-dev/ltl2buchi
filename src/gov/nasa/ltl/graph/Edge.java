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
public class Edge<PropT> {
  private Node<PropT>       source;
  private Node<PropT>       next;
  private Guard<PropT> guard;
  private String     action;
  private Attributes attributes;

  public Edge (Node<PropT> s, Node<PropT> n, Guard<PropT> g, String a, Attributes as) {
    init(s, n, g, a, as);
  }

  public Edge (Node<PropT> s, Node<PropT> n, Guard<PropT> g, String a) {
    init(s, n, g, a, null);
  }

  public Edge (Node<PropT> s, Node<PropT> n, Guard<PropT> g) {
    init(s, n, g, "-", null);
  }

  public Edge (Node<PropT> s, Node<PropT> n) {
    init(s, n, new Guard<PropT> (), "-", null);
  }

  public Edge (Node<PropT> s, Edge<PropT> e) {
    init(s, e.next, e.guard, e.action, 
         new Attributes(e.attributes));
  }

  public Edge (Edge<PropT> e, Node<PropT> n) {
    init(e.source, n, e.guard, e.action, 
         new Attributes(e.attributes));
  }

  public Edge (Edge<PropT> e) {
    init(e.source, e.next, e.guard, e.action, 
         new Attributes(e.attributes));
  }

  public String getAction () {
    return action;
  }

  public synchronized void setAttributes (Attributes a) {
    attributes = new Attributes(a);
  }

  public Attributes getAttributes () {
    return attributes;
  }

  public void setBooleanAttribute (String name, boolean value) {
    attributes.setBoolean(name, value);
  }

  public boolean getBooleanAttribute (String name) {
    return attributes.getBoolean(name);
  }

  public Guard<PropT> getGuard () {
    return guard;
  }

  public void setIntAttribute (String name, int value) {
    attributes.setInt(name, value);
  }

  public int getIntAttribute (String name) {
    return attributes.getInt(name);
  }

  public Node<PropT> getNext () {
    return next;
  }

  public Node<PropT> getSource () {
    return source;
  }

  public void setStringAttribute (String name, String value) {
    attributes.setString(name, value);
  }

  public String getStringAttribute (String name) {
    return attributes.getString(name);
  }

  public synchronized void remove () {
    source.removeOutgoingEdge(this);
    next.removeIncomingEdge(this);
  }

  private void init (Node<PropT> s, Node<PropT> n,
      Guard<PropT> g, String a, Attributes as) {
    source = s;
    next = n;
    guard = g;
    action = a;

    if (as == null) {
      attributes = new Attributes();
    } else {
      attributes = as;
    }

    s.addOutgoingEdge(this);
    n.addIncomingEdge(this);
  }
}
