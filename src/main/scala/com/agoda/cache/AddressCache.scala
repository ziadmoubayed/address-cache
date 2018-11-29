package com.agoda.cache

import java.net.InetAddress
import java.util.concurrent.locks.{Condition, ReadWriteLock, ReentrantReadWriteLock}

import com.agoda.cache.models.InetNode

import scala.collection.mutable.LinkedHashSet
import scala.concurrent.duration.TimeUnit

/*
 * The AddressCache has a max age for the elements it's storing, an add method
 * for adding elements, a remove method for removing, a peek method which
 * returns the most recently added element, and a take method which removes
 * and returns the most recently added element.
 */
class AddressCache(period: Long, unit: TimeUnit) extends Cache[InetAddress] {

  require(period > 0, "Cache Period should be a 'non zero - positive number'")

  private val ttl: Long = unit.toMillis(period)

  private val lock: ReadWriteLock = new ReentrantReadWriteLock()
  private val emptyCondition: Condition = lock.writeLock().newCondition()
  private val items: LinkedHashSet[InetNode] = LinkedHashSet[InetNode]()

  /**
    * add() method must store unique elements only (existing elements must be ignored).
    * This will return true if the element was successfully added.
    *
    * @param address
    * @return
    */
  override def add(address: InetAddress): Boolean = {
    val entry = InetNode(address, System.currentTimeMillis() + ttl)
    lock.writeLock().lock()
    try {
      clean()
      val added: Boolean = items.add(entry) //Return false is element already exists
      if (added) {
        emptyCondition.signal()
      }
      added
    } finally {
      lock.writeLock().unlock()
    }
  }


  /**
    * remove() method will return true if the address was successfully removed
    *
    * @param address
    * @return
    */
  override def remove(address: InetAddress): Boolean = {
    val entry = InetNode(address)
    lock.writeLock().lock()
    try {
      //removeOld()
      return items.remove(entry)
    } finally {
      lock.writeLock().unlock()
    }
  }

  /**
    * The peek() method will return the most recently added element,
    * None if no element exists.
    *
    * @return
    */
  override def peek(): Option[InetAddress] = {
    lock.readLock().lock()
    try {
      val last = items.lastOption
      if (last.isDefined && !last.get.isExpired) {
        return last map (_.address)
      }
    } finally {
      lock.readLock().unlock()
      clean()
    }
    None
  }

  /**
    * take() method retrieves and removes the most recently added element
    * from the cache and waits if necessary until an element becomes available.
    *
    * @return
    */
  override def take(): InetAddress = {
    lock.writeLock().lock()
    try {
      clean()
      while (items.isEmpty) {
        emptyCondition.await()
      }
      val last = items.last
      items.remove(last)
      last.address
    } finally {
      lock.writeLock().unlock()
    }
  }

  /**
    *
    */
  protected def clean(): Unit = {
    lock.writeLock().lock()
    try {
      while (items.nonEmpty && items.head.isExpired) {
        items.remove(items.head)
      }
    } finally {
      lock.writeLock().unlock()
    }
  }
}