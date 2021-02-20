package lecture022

import akka.NotUsed
import akka.stream.Graph
import akka.stream.scaladsl.GraphDSL.Implicits.FanInOps
import akka.stream.scaladsl.{Balance, GraphDSL, Merge}

object MultiBalance {

  def apply [T] ( numInputs: Int, numOutputs: Int ): Graph [ MultiBalanceShape [T], NotUsed ] = {

    GraphDSL.create () { implicit builder =>

      val merge = builder.add ( Merge [T] ( numInputs ) )
      val balance = builder.add ( Balance [T]  ( numOutputs ))

      merge ~> balance

      MultiBalanceShape [T] ( merge.inlets.toList, balance.outlets.toList )
    }
  }
}