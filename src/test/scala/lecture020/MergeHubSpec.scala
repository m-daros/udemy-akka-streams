package lecture020

import akka.actor.ActorSystem
import akka.stream.scaladsl.{MergeHub, Sink, Source}
import akka.stream.ActorMaterializer
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec

import scala.concurrent.duration.DurationInt


class MergeHubSpec extends AnyFeatureSpec with GivenWhenThen {

  implicit val actorSystem = ActorSystem ( "lecture020" )
  implicit val actorMaterializer = ActorMaterializer ()


  Feature ( "MergeHub" ) {

    Scenario ( "...." ) {

      Given ( "a MergeHub" )

      val dynamicMerge = MergeHub.source [Int]
      val materializedSink = dynamicMerge.to ( Sink.foreach [Int] ( println ) ).run ()


      When ( "...." )

      Source ( 1 to 10 ).throttle ( 1, 1 second )
        .runWith ( materializedSink )

      Source ( 91 to 100 ).throttle ( 1, 1 second )

        .runWith ( materializedSink )

      Then ( "...." )

      Thread.sleep ( 10 )

    }

  }
}