package lecture021

import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec

import java.util.Date
import scala.concurrent.duration.DurationInt
import scala.util.Random


class SubstreamsSpec extends AnyFeatureSpec with GivenWhenThen {

  implicit val actorSystem = ActorSystem ( "lecture021" )
  implicit val actorMaterializer = ActorMaterializer ()

  val metricGenerator = actorSystem.actorOf ( Props [MetricGenerator], "metric-generator" )


  Feature ( "Substreams" ) {

    Scenario ( "..." ) {

      Given ( "..." )

      val metricsSource = Source.tick ( 50 milliseconds, 20 milliseconds, "generateMetric" )
        .mapAsync (1 ) { _ =>

          implicit val timeout: Timeout = 3.seconds
          metricGenerator ? "generateMetric"
        }

      When ( "..." )

      // Generate metrcs for a set of systems with source
      // groupBy ( system ) to obtain subflow
      // Compute mean, variance every given timed window

      // TODO TMP
      metricsSource
        //.groupBy ( 5, metric => metric.system )
        .to ( Sink.foreach ( metric => println ( metric ) ) )
        .run ()

      Then ( "..." )

      Thread.sleep ( 10000 )
    }
  }
}