package lecture020

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{BroadcastHub, Keep, RunnableGraph, Sink, Source}
import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec

import scala.concurrent.duration.DurationInt


class BroadcastHubSpec extends AnyFeatureSpec with GivenWhenThen {

  implicit val actorSystem = ActorSystem ( "lecture020" )
  implicit val actorMaterializer = ActorMaterializer ()


  Feature ( "BroadcastHub" ) {

    Scenario ( "Broadcast messages with a BroadcastHub" ) {

      Given ( "a BroadcastHub" )

      val source = Source.tick ( 0 second, 1 second, 999 )
      val broadcast = source.toMat ( BroadcastHub.sink ) ( Keep.right ).run ()

      When ( "I attach some subscribers to it" )

      val sinkProbe1 = TestSink.probe [Int]
      val sinkProbe2 = TestSink.probe [Int]

      val testSink1 = broadcast.toMat ( sinkProbe1 ) ( Keep.right ).run ()
      val testSink2 = broadcast.toMat ( sinkProbe2 ) ( Keep.right ).run ()

      Then ( "I expect all the messages broadcasted by BroadcastHub will reach all the subscribers" )

      testSink1.request ( 5 )
        .expectNext ( 999, 999, 999, 999, 999 )

      testSink2.request ( 5 )
        .expectNext ( 999, 999, 999, 999, 999 )
    }
  }
}