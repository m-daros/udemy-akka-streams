package lecture020

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, KillSwitches}
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec

import scala.concurrent.duration.DurationInt

class DynamicStreamHandling extends AnyFeatureSpec with GivenWhenThen {

  implicit val actorSystem = ActorSystem ( "lecture020" )
  implicit val actorMaterializer = ActorMaterializer ()

  import actorSystem.dispatcher

  Feature ( "Dynamic Stream Handling" ) {

    Scenario ( "Handling shutdown of a single stream with KillSwitches" ) {

      Given ( "a throttling source emitting elements 1, 2, 3, 4, 5, ... every second" )

      val source = Source ( Stream.from ( 1 ) ).throttle ( 1, 1 second )

      And ( "a test sink" )

      val sinkProbe = TestSink.probe [Int]

      And ( "a KillSwitches flow" )

      val killSwitchFlow = KillSwitches.single [Int]

      And ( "a graph connecting them" )

      val graph = source
        .viaMat ( killSwitchFlow ) ( Keep.right )
        .toMat ( sinkProbe ) ( Keep.both )

      When ( "I run the graph" )

      val ( killSwitch, testSink ) = graph.run ()

      And ( "I shutdown the graph after 3 seconds" )

      actorSystem.scheduler.scheduleOnce ( 3 seconds ) {

        killSwitch.shutdown ()
      }

      Then ( "I expect the elements 1, 2, 3 on sink without completion" )

      testSink
        .request ( 10 )
        .expectNext ( 1 )
        .expectNext ( 2 )
        .expectNext ( 3 )
    }
  }
}