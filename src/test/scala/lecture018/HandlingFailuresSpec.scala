package lecture018

import akka.Done
import akka.actor.{Actor, ActorSystem, Props}
import akka.actor.Status.Failure
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, RestartSource, Sink, Source}
import akka.testkit.TestProbe
import akka.util.Timeout
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec

import scala.concurrent.duration.DurationInt
import scala.util.Random

class HandlingFailuresSpec extends AnyFeatureSpec with GivenWhenThen {

  implicit val actorSystem = ActorSystem ( "lecture018" )
  implicit val actorMaterializer = ActorMaterializer ()

  val testProbe = TestProbe ()

  Feature ( "Handling failures in Akka Streams" ) {

    Scenario ( "Logging the failure and let the stream stop" ) {

      Given ( "A failing source" )

      val failingSource = Source ( 1 to 20 ).map ( number => if ( number == 10 ) throw new RuntimeException else number )

      When ( "I run the graph" )

      failingSource.log ( "failingSource" )
        .to ( Sink.actorRef ( testProbe.ref, onCompleteMessage = Done ) )
        .run ()

      Then ( "I expect the stream will flow the elements before the failure" )

      for ( i <- 1 to 9 ) {

        testProbe.expectMsg ( i )
      }

      And ( "I expect the stream will fail" )
      testProbe.expectMsgType [ Failure ]
      testProbe.expectNoMessage ()
    }

    Scenario ( "Provide a value when the failure occurs and let the stream stop" ) {

      Given ( "A failing source with recovery" )

      val failingSource = Source ( 1 to 20 ).map ( number => if ( number == 10 ) throw new RuntimeException else number )
        .recover {

          case e: RuntimeException => 999
        }

      When ( "I run the graph" )

      failingSource.log ( "recoveringSource" )
        .to ( Sink.actorRef ( testProbe.ref, onCompleteMessage = Done ) )
        .run ()

      Then ( "I expect the stream will flow the elements before the failure" )

      for ( i <- 1 to 9 ) {

        testProbe.expectMsg ( i )
      }

      And ( "I expect the recovery value instead of the failure" )
      testProbe.expectMsg ( 999 )

      And ( "I expect the stream will not fail" )
      testProbe.expectMsg ( Done )
      testProbe.expectNoMessage ()
    }
  }
}