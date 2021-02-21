package lecture023

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

object CustomGraphStagesApp3 extends App {

  implicit val actorSystem = ActorSystem ( "lecture023" )
  implicit val actorMaterializer = ActorMaterializer ()

  val randomNumbersSource = Source.fromGraph ( new RandomIntGenerator ( 100 ) )
  val groupingSink = Sink.fromGraph ( new Grouper [Int] ( 10 ) )

  randomNumbersSource.runWith ( groupingSink )
}