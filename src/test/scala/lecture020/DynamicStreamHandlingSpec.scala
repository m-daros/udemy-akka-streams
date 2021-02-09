package lecture020

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, KillSwitches}
import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec

import scala.concurrent.duration.DurationInt

class DynamicStreamHandlingSpec extends AnyFeatureSpec with GivenWhenThen {

  implicit val actorSystem = ActorSystem ( "lecture020" )
  implicit val actorMaterializer = ActorMaterializer ()

  import actorSystem.dispatcher

  Feature ( "Dynamic Stream Handling" ) {

    Scenario ( "Handling shutdown of a single stream with KillSwitches.single ()" ) {

      Given ( "a throttling source emitting elements 1, 2, 3, 4, 5, ... every second" )

      val source = Source ( Stream.from ( 1 ) ).throttle ( 1, 1 second )

      And ( "a test sink" )

      val sinkProbe = TestSink.probe [Int]

      And ( "a KillSwitches.single () flow" )

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

    Scenario ( "Handling shutdown of mukltiple stream with KillSwitches.shared" ) {

      Given ( "a throttling source emitting elements 1, 2, 3, 4, 5, ... every second" )

      val source1 = Source ( Stream.from ( 1 ) ).throttle ( 1, 1 second )

      And ( "a throttling source emitting elements 1, 2, 3, 4, 5, ... every 1/2 second" )

      val source2 = Source ( Stream.from ( 1 ) ).throttle ( 2, 1 second )

      And ( "a couple of test sinks" )

      val sinkProbe1 = TestSink.probe [Int]
      val sinkProbe2 = TestSink.probe [Int]

      And ( "a KillSwitches.shared () flow" )

      val killSwitchFlow = KillSwitches.shared ( "shared-kill-flow" )

      And ( "a couple of graphs connecting them" )

      val graph1 = source1
        .via ( killSwitchFlow.flow )
        .log ( "graph1" )
        .toMat ( sinkProbe1 ) ( Keep.right )

      val graph2 = source2
        .via ( killSwitchFlow.flow )
        .log ( "graph2" )
        .toMat ( sinkProbe2 ) ( Keep.right )

      When ( "I run the graphs" )

      val testSink1 = graph1.run ()
      val testSink2 = graph2.run ()

      And ( "I shutdown the graphs after 3 seconds" )

      actorSystem.scheduler.scheduleOnce ( 3 seconds ) {

        killSwitchFlow.shutdown ()
      }

      testSink1
        .request ( 10 )

      testSink2
        .request ( 10 )

      Then ( "I expect the elements 1, 2, 3 on sink1 without completion" )

      testSink1
        .expectNext ( 1 )
        .expectNext ( 2 )
        .expectNext ( 3 )

      And ( "I expect the elements 1, 2, 3, 4, 5, 6 on sink2 without completion" )

      testSink2
        .expectNext ( 1 )
        .expectNext ( 2 )
        .expectNext ( 3 )
        .expectNext ( 4 )
        .expectNext ( 5 )
        .expectNext ( 6 )
    }
  }
}