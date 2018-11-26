package endpoints.akkahttp.server

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import endpoints.algebra
import endpoints.algebra.CirceEntities.CirceCodec

/**
  * Implements [[algebra.CirceEntities]] for [[Endpoints]].
  */
trait CirceEntities extends Endpoints with algebra.CirceEntities {

  import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._

  def jsonRequest[A: CirceCodec] = {
    implicit val decoder = CirceCodec[A].decoder
    Directives.entity[A](implicitly[FromRequestUnmarshaller[A]])
  }

  def jsonResponse[A: CirceCodec] = a => {
    implicit val encoder = implicitly[CirceCodec[A]].encoder
    Directives.complete(a)
  }

}
