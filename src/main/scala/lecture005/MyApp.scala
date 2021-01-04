package lecture005

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source

object MyApp extends App {

  // Exercise: create a stream that take the names of persons, then keep the first 2 names with length > 5 chars
  implicit val actorSystem = ActorSystem ( "lecture005" )
  implicit val actorMaterializer = ActorMaterializer ()

  Source ( List ( "Valeria", "Aldo", "Giovanni", "Giacomo", "Sofia", "Lucia", "Roberto", "Marco" ) )
    .filter ( name => name.length > 5 )
    .take ( 2 )
    .runForeach ( println )
}