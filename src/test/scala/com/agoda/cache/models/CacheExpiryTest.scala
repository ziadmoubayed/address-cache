package com.agoda.cache.models

import java.net.InetAddress

import com.agoda.cache.AddressCache
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.duration._

class CacheExpiryTest extends FlatSpec with Matchers {

  "An AddressCache" should "remove elements when they expire & block when empty" in {
    val cache = new AddressCache(1, SECONDS)
    cache.add(InetAddress.getByName("7.8.9.10"))
    Thread.sleep(2.seconds.toMillis)
    cache.peek() shouldBe None
    val taker = new Thread(new Runnable {
      override def run(): Unit = {
        cache.take()
      }
    })
    taker.setDaemon(true)
    taker.start
    //TODO this is dangerous here.
    Thread.sleep(1.seconds.toMillis)
    taker.getState() should be(Thread.State.WAITING)
  }

  it should "return the same elements that were written" in {
    val cache = new AddressCache(1, MINUTES)
    val num = 50
    val addrs = Vector.tabulate(num)(i => InetAddress.getByName(s"1.2.3.$i"))
    addrs foreach cache.add
    val took = Vector.fill(num)(cache.take()).reverse
    addrs.sameElements(took) shouldBe true
  }

  //TODO
  //  it should "calling add on an existing element should update its expiry" in {
  //    val addr = InetAddress.getLoopbackAddress
  //    val cache = new AddressCache(50, MILLISECONDS)
  //    cache.add(addr)
  //    Thread.sleep(25)
  //    cache.add(addr)
  //    Thread.sleep(35)
  //    assert(cache.peek().isDefined)
  //  }
}
