package lecture023

import akka.stream.{Attributes, Inlet, SinkShape}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler}
import sun.security.ec.point.ProjectivePoint.Mutable

import scala.collection.mutable

class Grouper [T] ( groupSize: Int ) extends GraphStage [SinkShape [T]] {

  val group  = new mutable.Queue [T] ()
  val inPort: Inlet [T] = Inlet [T] ( "grouper" )

  override def shape: SinkShape [T] = {

    SinkShape [T] ( inPort )
  }

  override def createLogic ( inheritedAttributes: Attributes ): GraphStageLogic = {

    new GraphStageLogic ( shape ) {

      override def preStart (): Unit = {

        pull ( inPort )
      }

      setHandler ( inPort, new InHandler {

        override def onPush (): Unit = {

          group.enqueue ( grab ( inPort ) )

          if ( group.size >= groupSize ) {

            println ( "group: " + group.dequeueAll ( _ => true ).mkString ( "[", ", ", "]" ) )
          }

          pull ( inPort )
        }

        override def onUpstreamFinish (): Unit = {

          if ( group.nonEmpty ) {

            println ( "group: " + group.dequeueAll ( _ => true ).mkString ( "[", ", ", "]" ) )
          }

          println ( "The stream is terminated" )
        }
      } )
    }
  }
}