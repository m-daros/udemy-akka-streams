package lecture021

import akka.actor.{Actor, TypedActor}

import java.util.Date
import scala.util.Random

class MetricGenerator extends Actor {

  private val systems = List ( "system-01", "system-02", "system-03", "system-04", "system-05" )

  private val random = new Random

  private def getSystem (): String = {

    systems ( ( random.nextFloat () * systems.size - 1 ).ceil.toInt )
  }

  private def getMeasure (): Float = {

    10 * random.nextFloat ()
  }

  override def receive: Receive = {

    case _ => {

      sender () ! Metric ( getSystem (), new Date (), getMeasure () )
    }
  }
}