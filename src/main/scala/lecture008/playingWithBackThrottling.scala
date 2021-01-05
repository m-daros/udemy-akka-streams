package lecture008

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.duration.DurationInt

object playingWithBackThrottling extends App {

  implicit val actorSystem = ActorSystem ( "lecture008" )
  implicit val actorMaterializer = ActorMaterializer ()

  val numbersSource = Source ( 1 to 1000 )

  val flow1 = Flow [Int].map (number => {

    val result = number + 1
    println ( s"slowFlow1 - Processed number: $number, producing result:  $result" )

    result
  } )

  val flow2 = Flow [Int].map (number => {

    val result = number * 2
    println ( s"slowFlow2 - Processed number: $number, producing result:  $result" )

    result
  } )

  val sink = Sink.foreach [Int] (number => {

    println ( s"slowSink - Result: $number" )
  } )

  numbersSource.throttle ( 2, 1 second )
    .via ( flow1 ).async
    .via ( flow2 ).async
    .to ( sink )
    .run ()
}