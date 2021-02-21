package lecture023

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

object CustomGraphStagesApp1 extends App {

  implicit val actorSystem = ActorSystem ( "lecture023" )
  implicit val actorMaterializer = ActorMaterializer ()

  val randomNumbersSource = Source.fromGraph ( new RandomIntGenerator ( 100 ) )
  val printValuesSink = Sink.foreach [Int] ( n => println ( s"n: $n" ) )

  randomNumbersSource.runWith ( printValuesSink )
}