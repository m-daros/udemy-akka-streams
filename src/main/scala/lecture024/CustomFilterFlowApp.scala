package lecture024

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

object CustomFilterFlowApp extends App {

  implicit val actorSystem = ActorSystem ( "lecture024" )
  implicit val actorMaterializer = ActorMaterializer ()

  val filter: Int => Boolean = i => i % 2 == 0

  val filterFlow = new CustomFilterFlow [Int] ( filter )

  val source = Source ( 1 to 100 )
  val sink = Sink.foreach [Int] ( i => println ( s"$i" ) )

  source.via ( filterFlow )
    .to ( sink )
    .run ()
}