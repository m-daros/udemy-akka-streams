package lecture020

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, MergeHub, Sink, Source}
import akka.stream.ActorMaterializer
import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec

import scala.concurrent.duration.DurationInt


class MergeHubSpec extends AnyFeatureSpec with GivenWhenThen {

  implicit val actorSystem = ActorSystem ( "lecture020" )
  implicit val actorMaterializer = ActorMaterializer ()


  Feature ( "MergeHub" ) {

    Scenario ( "Attach multiple sources using a MergeHub" ) {

      Given ( "a MergeHub" )

      val sinkProbe = TestSink.probe [Int]
      val dynamicMerge = MergeHub.source [Int]
      val ( materializedSink, testSink ) = dynamicMerge.toMat ( sinkProbe ) ( Keep.both ).run ()

      When ( "I attach multiple sources to it" )

      Source ( 1 to 10 )
        .runWith ( materializedSink )

      Source ( 91 to 100 )
        .runWith ( materializedSink )

      Then ( "I expect all the messages that will provided by the sources will flow to the MergeHub and reach the subscriber sink" )

      testSink.request ( 20 )
        .expectNextUnordered ( 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100 )
    }
  }
}