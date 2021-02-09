package lecture019

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.testkit.TestProbe
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec

import scala.util.{Failure, Success}

class TestingStreamsSpec extends AnyFeatureSpec with GivenWhenThen {

  implicit val actorSystem = ActorSystem ( "lecture019" )
  implicit val actorMaterializer = ActorMaterializer ()

  val testProbe = TestProbe ()

  Feature ( "Testing streams in Akka Streams" ) {

    Scenario ( "Test using a TestSink probe" ) {

      Given ( "a source" )

      val source = Source ( 1 to 10 ).map ( n => n * 2 )

      And ( "a TestSink" )

      val testSink = TestSink.probe [ Int ]

      When ( "I run the stream" )

      val materializedValue = source.runWith ( testSink )

      Then ( "I exoect 10 values" )

      materializedValue
        .request ( 10 )
        .expectNext ( 2, 4, 6, 8, 10, 12, 14, 16, 18, 20 )
        .expectComplete ()
    }

    Scenario ( "Test using a TestSource probe" ) {

      import actorSystem.dispatcher

      Given ( "a sink" )

      val sinkUnderTest = Sink.foreach [Int] {

        case 13 => new RuntimeException ()

        case n => println ( n )
      }

      And ( "a TestSource" )

      val sourceProbe = TestSource.probe [Int]

      When ( "I run the stream" )

      val materializedValue = sourceProbe.toMat ( sinkUnderTest ) ( Keep both ).run ()

      val ( testPublisher, resultFuture ) = materializedValue

      Then ( "I exoect it fail" )

      testPublisher
        .sendNext (1 )
        .sendNext ( 2 )
        .sendNext ( 13 )
        .sendComplete ()

      resultFuture.onComplete {

        case Success ( _ ) => fail ( "The sink under test should fail at 13" )

        case Failure ( _ ) => // OK
      }
    }

    Scenario ( "Test using TestSource and TestSink probes to test a flow" ) {

      Given ( "a flow" )

      val flowUnderTest = Flow [Int].map ( _ * 2 )

      And ( "a TestSource" )

      val sourceProbe = TestSource.probe [Int]

      And ( "a TestSink" )

      val sinkProbe = TestSink.probe [Int]

      When ( "I run the graph having sourceProbe -> flow -> sinkProbe" )

      val materialized = sourceProbe.via ( flowUnderTest ).toMat ( sinkProbe ) ( Keep.both ).run ()

      val ( publisher, subscriber ) = materialized

      And ( "I generate some values using the source and then I complete the flow" )

      publisher
        .sendNext ( 1 )
        .sendNext ( 2 )
        .sendNext ( 3 )
        .sendNext ( 4 )
        .sendNext ( 5 )
        .sendComplete()

      Then ( "I expect the values will arrive to the sink after trasformation by the flow and the sream will complete corresponding to completgion send by the source" )

      subscriber
        .request ( 5 )
        .expectNext ( 2 )
        .expectNext ( 4 )
        .expectNext ( 6 )
        .expectNext ( 8 )
        .expectNext ( 10 )
        .expectComplete()
    }
  }
}