package lecture023

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

object CustomGraphStagesApp2 extends App {

  implicit val actorSystem = ActorSystem ( "lecture023" )
  implicit val actorMaterializer = ActorMaterializer ()

  val source = Source ( 1 to 100 )
  val groupingSink = Sink.fromGraph ( new Grouper [Int] ( 10 ) )

  source.runWith ( groupingSink )
}