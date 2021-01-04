package lecture006

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink, Source}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

object MyApp extends App {

  /* Exercise:
    - Return the last element out of a source (use Sink.last)
    - Compute the total word count out of a stream of sentences (use map, fold, reduce)
   */
  implicit val actorSystem = ActorSystem ( "lecture006" )
  implicit val actorMaterializer = ActorMaterializer ()

  val sentences = List ( "Nel mezzo del cammin di nostra vita",
    "mi ritrovai per una selva oscura",
    "che la diritta via era smarrita" )

  val lastSentenceFuture1 = Source ( sentences )
    .toMat ( Sink.last ) ( Keep.right ).run ()

  val lastSentenceFuture2 = Source ( sentences )
    .runWith ( Sink.last )

  val totalLenghtFuture1 = Source ( sentences )
    .map ( sentence => sentence.split ( "\\s" ).length )
    .toMat ( Sink.reduce [Int] ( ( lenght1, lenght2 ) => lenght1 + lenght2 ) ) ( Keep.right ).run ()

  val totalLenghtFuture2 = Source ( sentences )
    .toMat ( Sink.fold [Int, String] ( 0 ) ( ( accumulator, sentence ) => accumulator + sentence.split ( "\\s" ).length ) ) ( Keep.right ).run ()

  val totalLenghtFuture3 = Source ( sentences )
    .runFold ( 0 ) ( ( accumulator, sentence ) => accumulator + sentence.split ( "\\s" ).length )

  lastSentenceFuture1.onComplete { value =>

    println ( s"Variant 1: the last sentence is: ${value.get}" )
  }

  lastSentenceFuture2.onComplete { value =>

    println ( s"Variant 2: the last sentence is: ${value.get}" )
  }

  totalLenghtFuture1.onComplete { value =>

    println ( s"Variant 1: the total words count is: ${value.get}" )
  }

  totalLenghtFuture2.onComplete { value =>

    println ( s"Variant 2: the total words count is: ${value.get}" )
  }

  totalLenghtFuture3.onComplete { value =>

    println ( s"Variant 3: the total words count is: ${value.get}" )
  }

  actorSystem.scheduler.scheduleOnce ( 5 second, new Runnable () {

    override def run (): Unit = actorSystem.terminate ()
  } )
}