package lecture014

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ClosedShape, UniformFanInShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, MergePreferred, RunnableGraph, Sink, Source, Zip}

object Fibonacci extends App {

  /*
    Challenge: create a fan-in shape
    - two inputs which will be fed with EXACTLY ONE number (1 and 1)
    - output will emit an INFINITE FIBONACCI SEQUENCE based of those two numbers
    1, 2, 3, 5, 8, ....

    Hint: use ZipWith and cycles, MergePreferred
   */

  /*
    Fibonacci sequence: F0 = 0, F1 = 1, Fn = Fn-1 + Fn-2 with n > 1
    0, 1, 1, 2, 3, 5, 8, ....
   */
  implicit val actorSystem = ActorSystem ( "lecture014" )
  implicit val actorMAterializer = ActorMaterializer ()

  val source0 = Source.single [ BigInt ] ( 1 )
  val source1 = Source.single [ BigInt ] ( -1 )

  val sink = Sink.foreach [ BigInt ] ( value => println ( value ) )

  val fibonacciGraph = GraphDSL.create () { implicit builder =>

    import GraphDSL.Implicits._

    val mergeShape = builder.add ( MergePreferred [ ( BigInt, BigInt ) ] (1 ) )
    val broadcastShape = builder.add ( Broadcast [ ( BigInt, BigInt ) ] (2 ) )

    val zipShape = builder.add ( Zip [ BigInt, BigInt ] )

    val fibonacciShape = builder.add ( Flow [ ( BigInt, BigInt ) ].map ( pair => {

      val last = pair._1
      val previous = pair._2

      Thread.sleep ( 100 )

      ( previous + last, last )
    } ) )

    val elementExtractorShape = builder.add ( Flow [ ( BigInt, BigInt ) ].map ( pair => {

      val last = pair._1
      val previous = pair._2

      last
    } ) )

    zipShape.out ~> mergeShape ~> fibonacciShape ~> broadcastShape ~> elementExtractorShape
                    mergeShape.preferred     <~     broadcastShape

    UniformFanInShape ( elementExtractorShape.out, zipShape.in0, zipShape.in1 )
  }

  val graph = RunnableGraph.fromGraph ( GraphDSL.create () { implicit builder =>

    import GraphDSL.Implicits._

    val source0Shape    = builder.add ( source0 )
    val source1Shape    = builder.add ( source1 )
    val fibonacchiShape = builder.add ( fibonacciGraph )
    val sinkShape       = builder.add ( sink )

    source0Shape ~> fibonacchiShape
    source1Shape ~> fibonacchiShape

    fibonacchiShape ~> sinkShape

    ClosedShape
  } )

  graph.run ()
}