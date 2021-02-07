package lecture019

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import akka.testkit.TestProbe
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec

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
  }
}