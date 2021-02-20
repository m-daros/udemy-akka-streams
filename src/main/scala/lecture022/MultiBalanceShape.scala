package lecture022

import akka.stream.{Inlet, Outlet, Shape}

case class MultiBalanceShape [T] ( override val inlets: List [Inlet [T]], override val outlets: List [Outlet [T]] ) extends Shape {

  override def deepCopy (): Shape = {

    MultiBalanceShape ( inlets.map ( inlet => inlet.carbonCopy () ), outlets.map ( outlet => outlet.carbonCopy () ) )
  }
}