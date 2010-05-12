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

/**
 * Counter which generates IDs for Node instances.
 */
class Pool {
  private int     last_assigned = 0;
  private boolean stopped = false;

  /**
   * Obtain new ID. Should not be called if this Pool instance
   * has been stopped.
   * @return new (distinct) ID
   */
  int requestId () {
    assert !stopped : "ID requested, but Pool is already stopped";
    return last_assigned++;
  }
  
  /**
   * Return the last ID returned by {@link Pool#requestId()}.
   * Only works if that method has actually been called at
   * least once.
   * @return last ID
   */
  int lastId () {
    assert last_assigned > 0 : "no IDs assigned yet";
    return last_assigned - 1;
  }

  /**
   * Get the number of IDs generated so far. Only works if
   * this Pool instance has been stopped.
   * @return number of generated IDs
   */
  int getIdCount () {
    assert stopped : "not done assigning IDs yet";
    return last_assigned;
  }

  /**
   * Stop this Pool instance. This makes calls to {@link Pool#requestId()}
   * invalid and calls to {@link Pool#getIdCount()} valid.
   */
  void stop () {
    assert !stopped : "Pool is already stopped";
    stopped = true;
  }
}
