package com.agoda.cache.models

import java.net.InetAddress
import java.time.Instant

import scala.concurrent.duration._
import org.scalatest.FunSuite

class InetNodeEqualityTest extends FunSuite {

  val ttl = 2.seconds.toMillis

  // these first two instances should be equal
  val add1 = InetNode(InetAddress.getByName("localhost"))
  val add2 = InetNode(InetAddress.getByName("localhost"), System.currentTimeMillis() + ttl)
  val add3 = InetNode(InetAddress.getByName("10.10.10.10"))
  val add4 = InetNode(InetAddress.getByName("10.10.10.10"), System.currentTimeMillis() + ttl)

  test("add1 == add1")   { assert(add1 == add1) }
  test("add1 == add2")   { assert(add1 == add2) }
  test("add2 == add1")   { assert(add2 == add1) }
  test("add1 != add3")   { assert(add1 != add3) }
  test("add3 != add1")   { assert(add3 != add1) }
  test("add1 != null")   { assert(add1 != null) }
  test("add1 != add4")   { assert(add1 != add4) }
  test("add1 != \"localhost\"")  { assert(add1 != "localhost") }
}