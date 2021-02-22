package lecture024

import akka.stream.{Attributes, FlowShape, Inlet, Outlet, SinkShape}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}

class CustomFilterFlow [T] ( predicate: T => Boolean ) extends GraphStage [FlowShape [T, T]] {

  val inPort = Inlet [T] ( "inpout" )
  val outPort = Outlet [T] ( "output" )

  override def shape: FlowShape [T, T] = {

    FlowShape [T, T] ( inPort, outPort )
  }

  override def createLogic ( inheritedAttributes: Attributes ): GraphStageLogic = {

    new GraphStageLogic ( shape ) {

//      override def preStart (): Unit = {
//
//        pull ( inPort )
//      }

      setHandler ( outPort, new OutHandler () {

        override def onPull (): Unit = {

            pull ( inPort )
        }
      } )

      setHandler ( inPort, new InHandler {

        override def onPush (): Unit = {

          try {

            val element = grab ( inPort )

            if ( predicate ( element ) ) {

              push ( outPort, element ) // Let the element to pass
            }
            else {

              pull ( inPort ) // Ask another element
            }
          }
          catch {

            case e: Throwable => failStage ( e )
          }
        }
      } )

    }
  }

}