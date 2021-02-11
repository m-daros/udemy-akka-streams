package lecture020

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{BroadcastHub, Keep, MergeHub, Sink, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec


class PublishSubscribeSpec extends AnyFeatureSpec with GivenWhenThen {

  implicit val actorSystem = ActorSystem ( "lecture020" )
  implicit val actorMaterializer = ActorMaterializer ()


  Feature ( "Publish / Subscribe with MergeHub and BroadcastHub" ) {

    Scenario ( "Implementing a pusblish / subscribe mechanism" ) {

      Given ( "a MergeHub" )

      val mergeHub = MergeHub.source [String]

      And ( "a BroadcastHub" )

      val broadcastHub = BroadcastHub.sink [String]

      When ( "I link the MergeHub and the BroadcastHub" )

      val ( publisherPort, subscriberPort ) = mergeHub.toMat ( broadcastHub ) ( Keep.both ).run ()

      Then ( "I expect I can plug multipole sources and multiple sinks having a publisher / subscriber mechanism" )

      val sourceProbe1 = TestSource.probe [String]
      val sourceProbe2 = TestSource.probe [String]
      val sourceProbe3 = TestSource.probe [String]

      val sinkProbe1 = TestSink.probe [String]
      val sinkProbe2 = TestSink.probe [Int]

      val testSink1 = subscriberPort.toMat ( sinkProbe1 ) ( Keep.right ).run ()
      val testSink2 = subscriberPort.map ( word => word.length ).toMat ( sinkProbe2 ) ( Keep.right ).run ()

      val testSource1 = sourceProbe1.toMat ( publisherPort ) ( Keep.left ).run ()
      val testSource2 = sourceProbe2.toMat ( publisherPort ) ( Keep.left ).run ()
      val testSource3 = sourceProbe3.toMat ( publisherPort ) ( Keep.left ).run ()

      // Generate some messages for the attached sources that are send to the publish / subscribe
      testSource1.sendNext ( "Akka" )
        .sendNext ( "is" )
        .sendNext ( "awesome" )
        .sendComplete ()

      testSource2.sendNext ( "I" )
        .sendNext ( "love" )
        .sendNext ( "Scala" )
        .sendComplete ()

      testSource3.sendNext ( "Bye" )
        .sendComplete ()

      And ( "I expect all the messages collected by the publish / subscribe will reach all the subscribers" )

      testSink1.request ( 10 )
        .expectNext ( "Akka" )
        .expectNext ( "is" )
        .expectNext ( "awesome" )
        .expectNext ( "I" )
        .expectNext ( "love" )
        .expectNext ( "Scala" )
        .expectNext ( "Bye" )

      testSink2.request ( 10 )
        .expectNext ( 4 )
        .expectNext ( 2 )
        .expectNext ( 7 )
        .expectNext ( 1 )
        .expectNext ( 4 )
        .expectNext ( 5 )
        .expectNext ( 3 )
    }
  }
}