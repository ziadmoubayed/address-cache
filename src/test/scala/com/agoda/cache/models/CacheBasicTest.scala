package com.agoda.cache.models

import java.net.InetAddress

import com.agoda.cache.AddressCache
import org.scalatest._

import scala.concurrent.{Await, Future, TimeoutException}

class CacheBasicTest extends FlatSpec with Matchers {

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._

  "An AddressCache" should "return values in last-in-first-out order" in {
    val cache = new AddressCache(1, MINUTES)
    cache.add(InetAddress.getByName("1.2.3.4"))
    cache.add(InetAddress.getByName("3.4.5.6"))
    cache.add(InetAddress.getByName("7.8.9.10"))

    cache.take() should be(InetAddress.getByName("7.8.9.10"))
    cache.take() should be(InetAddress.getByName("3.4.5.6"))
    cache.take() should be(InetAddress.getByName("1.2.3.4"))
  }


  "Add" should "return true if element is new" in {
    val cache = new AddressCache(1, MINUTES)
    cache.add(InetAddress.getByName("1.2.3.4")) should be(true)
  }

  it should "return false if element exits" in {
    val cache = new AddressCache(1, MINUTES)
    cache.add(InetAddress.getByName("1.2.3.4")) should be(true)
    cache.add(InetAddress.getByName("1.2.3.4")) should be(false)
  }

  "Remove" should "return true if element was removed" in {
    val cache = new AddressCache(1, MINUTES)
    cache.add(InetAddress.getByName("1.2.3.4")) should be(true)
    cache.remove(InetAddress.getByName("1.2.3.4")) should be(true)
    cache.peek() shouldBe None
  }

  it should "return false if element does not exist" in {
    val cache = new AddressCache(1, MINUTES)
    cache.add(InetAddress.getByName("1.2.3.4")) should be(true)
    cache.remove(InetAddress.getByName("1.2.3.40")) should be(false)
    cache.peek() shouldBe Some(InetAddress.getByName("1.2.3.4"))
  }

  "Peek" should "return last value but does not remove it" in {
    val cache = new AddressCache(1, MINUTES)
    cache.add(InetAddress.getByName("1.2.3.4"))
    cache.peek() should be(Some(InetAddress.getByName("1.2.3.4")))
    cache.peek() should be(Some(InetAddress.getByName("1.2.3.4")))
    cache.peek() should be(Some(InetAddress.getByName("1.2.3.4")))
    cache.peek() should be(Some(InetAddress.getByName("1.2.3.4")))

    cache.take() should be(InetAddress.getByName("1.2.3.4"))
  }

  it should "return None when empty" in {
    val cache = new AddressCache(1, MINUTES)
    cache.add(InetAddress.getByName("1.2.3.4"))
    cache.peek() should be(Some(InetAddress.getByName("1.2.3.4")))
    cache.take() should be(InetAddress.getByName("1.2.3.4"))
    cache.peek() should be(None)
  }

  "Take" should "return & remove last element added" in {
    val cache = new AddressCache(1, SECONDS)
    cache.add(InetAddress.getByName("1.2.3.4"))
    cache.take() should be(InetAddress.getByName("1.2.3.4"))
    cache.peek() should be(None)
  }

  it should "block if cache is empty" in {
    val cache = new AddressCache(1, SECONDS)
    cache.add(InetAddress.getByName("1.2.3.4"))
    Thread.sleep(2000)
    val f = Future {
      cache.take()
    }
    a[TimeoutException] should be thrownBy Await.result(f, 3.seconds)
  }
}
