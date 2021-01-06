package lecture009

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape}
import akka.stream.scaladsl.{Balance, Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source}

object Exercise1 extends App {

  implicit val actorSystem = ActorSystem ( "lecture009" )
  implicit val actorMaterializer = ActorMaterializer ()

  // Excercise 1: feed a source into 2 sinks at the same time (hint: use a broadcast)

  val source = Source ( 1 to 100 )

  val sink1 = Sink.foreach [Int] { value =>

    println ( s"sink1. the value is: $value" )
  }

  val sink2 = Sink.foreach [Int] { value =>

    println ( s"sink2. the value is: $value" )
  }

  val twoSinksGraph = RunnableGraph.fromGraph ( GraphDSL.create () { implicit builder: GraphDSL.Builder [NotUsed] =>

    import GraphDSL.Implicits._

    val broadcast = builder.add ( Broadcast [Int] ( 2 ) )

    source ~> broadcast
    broadcast.out ( 0 ) ~> sink1
    broadcast.out ( 1 ) ~> sink2

    ClosedShape
  } )

  twoSinksGraph.run ()
}