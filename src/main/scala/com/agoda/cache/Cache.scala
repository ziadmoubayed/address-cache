package com.agoda.cache

/*
 * The AddressCache has a max age for the elements it's storing, an add method
 * for adding elements, a remove method for removing, a peek method which
 * returns the most recently added element, and a take method which removes
 * and returns the most recently added element.
 */
trait Cache[T] {

  /**
    * add() method must store unique elements only (existing elements must be
    * ignored). This will return true if the element was successfully added.
    *
    * @param address
    * @return
    */
  def add(address: T): Boolean

  /**
    * remove() method will return true if the address was successfully removed
    *
    * @param address
    * @return
    */
  def remove(address: T): Boolean

  /**
    * The peek() method will return the most recently added element, null if no
    * element exists.
    *
    * @return
    */
  def peek(): Option[T]

  /**
    * take() method retrieves and removes the most recently added element from
    * the cache and waits if necessary until an element becomes available.
    *
    * @return
    */
  def take(): T
}
