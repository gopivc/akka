package io.akka.bench

import java.lang.System.nanoTime

import scala.concurrent.duration.DurationInt

import akka.actor.{ ActorRef, ActorSystem, Props, Actor }
import akka.actor.ActorDSL.inbox

import com.typesafe.config.ConfigFactory

object MultiRing extends App {

  val config = ConfigFactory.parseString("""
ring {
      fork-join-executor.parallelism-max = 8
}
  """)
  val system = ActorSystem("TheRing", config)

  val ready = inbox()(system)

  case object MakeDecrementer
  case class Successor(ref: ActorRef)
  case class Token(roundsLeft: Int)

  class RingNode extends Actor {
    var isDecrementer = false
    var successor: ActorRef = null
    def receive = {
      case MakeDecrementer               ⇒ isDecrementer = true
      case Successor(ref)                ⇒ successor = ref
      case t @ Token(0) if isDecrementer ⇒ ready.receiver ! t
      case Token(n) if isDecrementer     ⇒ successor ! Token(n - 1)
      case t: Token                      ⇒ successor ! t
    }
  }

  val N = 1000
  val M = 1000
  val last = N - 1

  val ring = (1 to N) map (i ⇒ system.actorOf(Props[RingNode].withDispatcher("ring"), "node" + i))
  val first = ring(0)
  first ! MakeDecrementer
  first ! Successor(ring(last))
  (1 to last) foreach (i ⇒ ring(i) ! Successor(ring(i - 1)))

  println("warming up")
  first ! Token(10 * M)
  ready.receive(10.seconds)

  println("starting test")
  for (K ← 1 to 20) {
    val start = nanoTime
    (0 until K) foreach (_ ⇒ first ! Token(M))
    (0 until K) foreach (_ ⇒ ready.receive(30.seconds))
    val time = nanoTime - start
    println(s"${N * M * K} messages took ${time}ns, processing ${1000000000L * N * M * K / time} msg/sec")
  }
  println("test finished")

  system.shutdown()

}