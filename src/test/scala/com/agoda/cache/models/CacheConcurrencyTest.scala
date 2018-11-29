package com.agoda.cache.models

import java.net.InetAddress
import java.util.concurrent.CountDownLatch

import com.agoda.cache.AddressCache
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.duration.{MINUTES, SECONDS, _}

class CacheConcurrencyTest extends FlatSpec with Matchers {

  "An AddressCache" should "not contain duplicates" in {
    val cache = new AddressCache(1, MINUTES)
    for (i <- 1 to 50) {
      cache.add(InetAddress.getByName("7.8.9.10"))
    }
    cache.peek() shouldBe Some(InetAddress.getByName("7.8.9.10"))
    cache.take() shouldBe InetAddress.getByName("7.8.9.10")
    cache.peek() shouldBe None
  }


  it should "writing and reading be thread safe" in {
    val cache = new AddressCache(1, MINUTES)
    val num = 5
    val addrs = Vector.tabulate(num)(i => InetAddress.getByName(s"1.2.3.$i")).toSet
    addrs.par foreach cache.add
    val took = (1 to num).par.map(_ => cache.take())
    addrs.size shouldEqual took.size
    took.foreach(addr => addrs.contains(addr) shouldBe true)
  }

  it should "unblock waiting thread when add is called" in {
    val cache = new AddressCache(1, SECONDS)
    val taker = new Thread(() => {
      cache.take()
    })
    taker.setDaemon(true)
    taker.start
    //TODO this is dangerous here.
    Thread.sleep(1.seconds.toMillis)
    taker.getState() should be(Thread.State.WAITING)
    cache.add(InetAddress.getByName("7.8.9.10"))
    Thread.sleep(1.seconds.toMillis)
    taker.getState() should be(Thread.State.TERMINATED)
  }

  it should "release all waiting threads if enough data is available" in {
    val cache = new AddressCache(1, MINUTES)
    val threads = 50

    val blocked = new CountDownLatch(threads)
    for (i <- 1 to threads) {
      val blocking = new Thread(() => {
        cache.take()
        blocked.countDown()
      }).start()
      val releasing = new Thread(() => {
        cache.add(InetAddress.getByName(s"1.2.3.$i"))
      }).start()
    }
    blocked.await(20, SECONDS)
    blocked.getCount shouldBe 0
  }
}
