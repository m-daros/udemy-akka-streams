package lecture008

import akka.actor.{ActorLogging, ActorSystem}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

object playingWithBackPressure extends App {

  implicit val actorSystem = ActorSystem ( "lecture008" )
  implicit val actorMaterializer = ActorMaterializer ()

  val numbersSource = Source ( 1 to 1000 )

  val slowFlow1 = Flow [Int].map ( number => {

    Thread.sleep ( 1000 )
    val result = number + 1
    println ( s"slowFlow1 - Processed number: $number, producing result:  $result" )

    result
  } )

  val slowFlow2 = Flow [Int].map ( number => {

    Thread.sleep ( 1000 )
    val result = number * 2
    println ( s"slowFlow2 - Processed number: $number, producing result:  $result" )

    result
  } )

  val slowSink = Sink.foreach [Int] ( number => {

    Thread.sleep ( 1000 )
    println ( s"slowSink - Result: $number" )
  } )

  numbersSource.via ( slowFlow1 ).async
    .via ( slowFlow2 ).async
    .to ( slowSink )
    .run ()
}