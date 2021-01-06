package lecture009

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Balance, Broadcast, GraphDSL, Merge, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, ClosedShape}

import scala.concurrent.duration.DurationInt

object Exercise2 extends App {

  implicit val actorSystem = ActorSystem ( "lecture009" )
  implicit val actorMaterializer = ActorMaterializer ()

  // Execrise 2: use Merge [Int] and Balance [Int]

  val source = Source ( 1 to 100 )

  val sink1 = Sink.fold [Int, Int] ( 0 ) ( ( count, value ) => {

    println ( s"sink1. Computed ${ count + 1 } elements, the value is: $value" )
    count + 1
  })

  val sink2 = Sink.fold [Int, Int] ( 0 ) ( ( count, value ) => {

    println ( s"sink2. Computed ${ count + 1 } elements, the value is: $value" )
    count + 1
  })

  val slowSource = source.throttle ( 5, 1 second )
  val fastSource = source.throttle ( 10, 1 second )

  val sourcesBalancedGraph = RunnableGraph.fromGraph ( GraphDSL.create () { implicit builder: GraphDSL.Builder[NotUsed] =>

    import GraphDSL.Implicits._

    val merge = builder.add ( Merge [Int] ( 2 ) )
    val balance = builder.add ( Balance [Int] ( 2 ) )

    slowSource ~> merge
    fastSource ~> merge

    merge ~> balance

    balance ~> sink1
    balance ~> sink2

    ClosedShape
  })

  sourcesBalancedGraph.run ()
}