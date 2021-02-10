package lecture020

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{BroadcastHub, Sink, Source}
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec

import scala.concurrent.Future

class BroadcastHubSpec extends AnyFeatureSpec with GivenWhenThen {

  implicit val actorSystem = ActorSystem ( "lecture020" )
  implicit val actorMaterializer = ActorMaterializer ()


  Feature ( "BroadcastHub" ) {

    Scenario ( "...." ) {

      Given ( "a BroadcastHub" )

      val dynamicBroadcast = BroadcastHub.sink [Int]
      val materializedsource = Source ( 1 to 20 ).runWith ( dynamicBroadcast )


      When ( "...." )

      val future1: Future [Done] = materializedsource
        .runWith ( Sink.foreach [Int] ( n => println ( s"graph1 $n" ) ) )

      val future2: Future [Done] = materializedsource
        .runWith ( Sink.foreach [Int] ( n => println ( s"graph2 $n" ) ) )

      Then ( "...." )

      import actorSystem.dispatcher

      future1.onComplete {

        value => println ( value )
      }

      future2.onComplete {

        value => println ( value )
      }
    }

  }
}