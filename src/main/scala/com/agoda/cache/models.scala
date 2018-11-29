package com.agoda.cache

import java.net.InetAddress

package object models {

  case class InetNode(address: InetAddress, expiry: Long = System.currentTimeMillis()) {
    def canEqual(a: Any) = a.isInstanceOf[InetNode]

    override def equals(that: Any): Boolean =
      that match {
        case that: InetNode => that.canEqual(this) && this.hashCode == that.hashCode
        case _ => false
      }

    override def hashCode: Int = {
      val prime = 31
      var result = 1
      result = prime * result + (if (address == null || address.getHostAddress() == null) 0
      else address.getHostAddress().hashCode)
      return result
    }

    /**
      *
      * @param node
      * @return
      */
    def isExpired(): Boolean = {
      return (expiry < System.currentTimeMillis())
    }
  }

}
