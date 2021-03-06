package lecture022

import akka.actor.ActorSystem
import akka.stream.scaladsl.GraphDSL.Implicits.port2flow
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.{GraphDSL, RunnableGraph, Sink, Source}

import scala.concurrent.duration.DurationInt

object CustomShapeApp extends App {

  implicit val actorSystem = ActorSystem ( "lecture022" )
  implicit val actorMaterializer = ActorMaterializer ()

  val graph1 = RunnableGraph.fromGraph (

    GraphDSL.create () {  implicit builder =>

      val multiBalance = MultiBalance [Int] ( 3, 4 )

      val source1 = builder.add ( Source ( 1 to 100 ).filter ( i => i % 2 == 0 ).throttle ( 5, 1 second ) )
      val source2 = builder.add ( Source ( 1 to 100 ).filter ( i => i % 2 == 1 ).throttle ( 5, 1 second ) )
      val source3 = builder.add ( Source ( 1 to 1000 ).filter ( i => i % 1000 == 0 ).throttle ( 5, 1 second ) )

      val sink1 = builder.add ( Sink.foreach [Int] ( i => println ( s"int sink 1: ${i}" ) ) )
      val sink2 = builder.add ( Sink.foreach [Int] ( i => println ( s"int sink 2: ${i}" ) ) )
      val sink3 = builder.add ( Sink.foreach [Int] ( i => println ( s"int sink 3: ${i}" ) ) )
      val sink4 = builder.add ( Sink.foreach [Int] ( i => println ( s"int sink 4: ${i}" ) ) )

      val multiBalanceShape = builder.add ( multiBalance )

      source1.out ~> multiBalanceShape.inlets (0)
      source2.out ~> multiBalanceShape.inlets (1)
      source3.out ~> multiBalanceShape.inlets (2)

      multiBalanceShape.outlets (0) ~> sink1
      multiBalanceShape.outlets (1) ~> sink2
      multiBalanceShape.outlets (2) ~> sink3
      multiBalanceShape.outlets (3) ~> sink4

      ClosedShape
    }
  )

  val graph2 = RunnableGraph.fromGraph (

    GraphDSL.create () {  implicit builder =>

      val multiBalance = MultiBalance [String] ( 2, 3 )

      val source1 = builder.add ( Source ( 1 to 100 ).filter ( i => i % 2 == 0 ).map ( i => "str-" + i ).throttle ( 5, 1 second ) )
      val source2 = builder.add ( Source ( 1 to 100 ).filter ( i => i % 2 == 1 ).map ( i => "str-" + i ).throttle ( 5, 1 second ) )

      val sink1 = builder.add ( Sink.foreach [String] ( str => println ( s"str sink 1: ${str}" ) ) )
      val sink2 = builder.add ( Sink.foreach [String] ( str => println ( s"str sink 2: ${str}" ) ) )
      val sink3 = builder.add ( Sink.foreach [String] ( str => println ( s"str sink 3: ${str}" ) ) )

      val multiBalanceShape = builder.add ( multiBalance )

      source1.out ~> multiBalanceShape.inlets (0)
      source2.out ~> multiBalanceShape.inlets (1)

      multiBalanceShape.outlets (0) ~> sink1
      multiBalanceShape.outlets (1) ~> sink2
      multiBalanceShape.outlets (2) ~> sink3

      ClosedShape
    }
  )

  graph1.run ()
  graph2.run ()
}