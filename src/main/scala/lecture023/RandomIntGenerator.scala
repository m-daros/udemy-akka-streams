package lecture023

import akka.stream.{Attributes, Outlet, SourceShape}
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler}

import scala.util.Random

class RandomIntGenerator ( max: Int ) extends GraphStage [SourceShape [Int]] {

  val random = new Random ()
  val outPort = Outlet [Int] ( "randomGenerator" )

  override def shape: SourceShape [Int] = {

    SourceShape [Int] ( outPort )
  }

  override def createLogic ( inheritedAttributes: Attributes ): GraphStageLogic = new GraphStageLogic ( shape ) {

    setHandler ( outPort, new OutHandler () {

      override def onPull (): Unit = {

        push ( outPort, random.nextInt ( max ) )
      }
    } )
  }
}