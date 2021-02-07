package lecture018

import akka.Done
import akka.actor.ActorSystem
import akka.actor.Status.Failure
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, RestartSource, Sink, Source}
import akka.testkit.TestProbe
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt

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

    Scenario ( "Restart the stream in case of failure" ) {

      Given ( "Given a failing source with restart policy with backoff" )

      var numAttempts = 1

      def simulateFailures ( n: Int ): Int = {

        n % ( 10 * numAttempts ) match {

          case 0 => {

            // TODO TMP
            println ( s"n: ${n} RuntimeException" )

            numAttempts = numAttempts + 1
            throw new RuntimeException
          }

          case _ => {

            // TODO TMP
            println ( s"n: ${n}" )

            n
          }
        }
      }

      val source = RestartSource.onFailuresWithBackoff (

        minBackoff = 1 second,
        maxBackoff = 65 seconds,
        randomFactor = 0.2
      ) ( () => Source ( 1 to 20 ).map ( value => simulateFailures ( value ) ) )

      val sink = Sink.fold ( 0 ) ( ( a: Int, b: Int ) => a + b )

      When ( "I run the stream" )

      import actorSystem.dispatcher

      val sumFuture: Future [Int] = source
        .log ( "restartingSource" )
        .toMat ( sink ) ( Keep.right )
        .run ()
        .pipeTo ( testProbe.ref )

      Then ( "I expect it restart after failure with an exponetial backoggthe sum is 445" )

      And ( "The sum is 445 ( first attempt: 1 to 9, second attempt: 1 to 19, third attempt: 1 to 20 )" )

      // First attempt:  1 + 2 + 3 + 4 + 5 + 6 + 7 + 8 + 9
      // Second attempt: 1 + 2 + 3 + 4 + 5 + 6 + 7 + 8 + 9 + 10 + 11 + 12 + 13 + 14 + 15 + 16 + 17 + 18 + 19
      // Third attempt:  1 + 2 + 3 + 4 + 5 + 6 + 7 + 8 + 9 + 10 + 11 + 12 + 13 + 14 + 15 + 16 + 17 + 18 + 19 + 20
      testProbe.expectMsg ( 5 seconds, 445 )
    }

    Scenario ( "Restart the stream in case of failure, intercepting values using a TestProbe actor as a sink" ) {

      Given ( "Given a failing source with restart policy with backoff" )

      var numAttempts = 1

      def simulateFailures ( n: Int ): Int = {

        n % ( 5 * numAttempts ) match {

          case 0 => {

            // TODO TMP
            println ( s"n: ${n} RuntimeException" )

            numAttempts = numAttempts + 1
            throw new RuntimeException
          }

          case _ => {

            // TODO TMP
            println ( s"n: ${n}" )

            n
          }
        }
      }

      val source = RestartSource.onFailuresWithBackoff (

        minBackoff = 1 second,
        maxBackoff = 60 seconds,
        randomFactor = 0.2
      ) ( () => Source ( 1 to 10 ).map ( value => simulateFailures ( value ) ) )

      val flow = Flow [Int].scan [Int] ( 0 ) ( ( a: Int, b: Int ) => a + b )

      val probeSink = Sink.actorRef ( testProbe.ref, "probe" )

      When ( "I run the stream" )

      source
        .log ( "restartingSource" )
        .via ( flow )
        .to ( probeSink )
        .run ()

      Then ( "I expect it restart after failure with an exponetial backoggthe sum is 445" )

      And ( "The sum is 445 ( first attempt: 1 to 9, second attempt: 1 to 19, third attempt: 1 to 20 )" )

      // First attempt:  1 + 2 + 3 + 4
      // Second attempt: 1 + 2 + 3 + 4 + 5 + 6 + 7 + 8 + 9
      // Third attempt:  1 + 2 + 3 + 4 + 5 + 6 + 7 + 8 + 9 + 10
      testProbe.expectMsgAllOf ( 5 seconds,0, /* first attempt */ 1, 3, 6, 10,  /* second attempt */ 11, 13, 16, 20, 25, 31, 38, 46, 55,  /* third attempt */  56, 58, 61, 65, 70, 76, 83, 91, 100, 110 )
    }
  }
}