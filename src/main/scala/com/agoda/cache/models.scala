package com.agoda.cache

import java.net.InetAddress

package object models {

  case class InetNode[A <: InetAddress](address :A, timestamp :Long){
    def canEqual(a: Any) = a.isInstanceOf[InetNode]
    override def equals(that: Any): Boolean =
      that match {
        case that: InetNode[A] => that.canEqual(this) && this.hashCode == that.hashCode
        case _ => false
      }
    override def hashCode: Int = {
      val prime = 31
      var result = 1
      result = prime * result + age;
      result = prime * result + (if (name == null) 0 else name.hashCode)
      return result
    }
  }
}
