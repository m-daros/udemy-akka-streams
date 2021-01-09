package lecture012

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, FlowShape}

import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

object MaterializeComplexGraphs extends App {

  implicit val actorSystem = ActorSystem ( "lecture012" )
  implicit val actorMAterializer = ActorMaterializer ()

  val evenNumbers = Source ( ( 1 to 100 ).filter ( _ % 2 == 0 ) )
  val oddNumbers = Source ( ( 1 to 100 ).filter ( _ % 2 == 1 ) )

  val printer = Sink.foreach [Int] { value =>

    println ( value )
  }

  val myFlow = Flow [Int].map ( _ * 10 )

  /*
      Exercise: define a method that takes a Flow and return the same Flow that returns as Materialized Value a Future [Int] the count of values that flows in it
      Hint: use a Broadcast and a Sink.fold
   */
  def enhanceFlow [ A, B ] ( flow: Flow [ A, B, _ ] ): Flow [ A, B, Future [ Int ] ] = {

    val counter = Sink.fold [ Int, B ] ( 0 ) ( ( count, value ) => count + 1 )

    Flow.fromGraph ( GraphDSL.create ( counter ) { implicit builder => counterShape =>

      import GraphDSL.Implicits._

      val flowShape      = builder.add ( flow )
      val broadcastShape = builder.add ( Broadcast [ B ] ( 2 ) )

      flowShape ~> broadcastShape ~> counterShape

      FlowShape ( flowShape.in, broadcastShape.out ( 1 ) )
    } )
  }

  val contFuture1: Future [ Int ] = oddNumbers.viaMat ( enhanceFlow ( myFlow ) ) ( Keep.right ).to ( printer ).run ()
  val contFuture2: Future [ Int ] = evenNumbers.viaMat ( enhanceFlow ( myFlow ) ) ( Keep.right ).to ( printer ).run ()

  import actorSystem.dispatcher

  contFuture1.onComplete {

    case Success ( count ) => println ( s"The graph evenNumbers -> flow -> printer received $count elements" )
    case Failure ( exception )  => println ( s"The graph evenNumbers -> flow -> printer failed due to $exception" )
  }

  contFuture2.onComplete {

    case Success ( count ) => println ( s"The graph oddNumbers -> flow -> printer received $count elements" )
    case Failure ( exception )  => println ( s"The graph oddNumbers -> flow -> printer failed due to $exception" )
  }
}