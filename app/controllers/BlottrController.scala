package controllers

import model.{BlottrRepo, BlottrQuery}
import play.api.mvc._
import services.BlottrConfig
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.duration._



case class CORSable[A](origins: String*)(action: Action[A]) extends Action[A] {

  def apply(request: Request[A]): Future[Result] = {

    val headers = request.headers.get("Origin").map { origin =>
      if(origins.contains(origin)) {
        List("Access-Control-Allow-Origin" -> origin, "Access-Control-Allow-Credentials" -> "true")
      } else { Nil }
    }

    action(request).map(_.withHeaders(headers.getOrElse(Nil) :_*))
  }

  lazy val parser = action.parser
}

object BlottrController extends Controller with PanDomainAuthActions {

  def hello = APIAuthAction {
    Ok("Hello from blottr")
  }

  def search = CORSable(BlottrConfig().composerUrl) {
    APIAuthAction { req =>

      val query = BlottrQuery(
        user = Some(req.user.email),
        composerId = req.getQueryString("composerId")
      )

      val blottrs = BlottrRepo.findBlottrs(query).map(_.asJson).mkString("[", ",", "]")

      Ok(blottrs).as(JSON)
    }
  }

  def addPersonalBlottr = CORSable(BlottrConfig().composerUrl) {
    APIAuthAction { req =>
      val user = req.user.email
      BlottrRepo.addUserBlottr(user)

      NoContent
    }
  }

  def putPayload(key: String) = CORSable(BlottrConfig().composerUrl) {
    APIAuthAction { req =>

      val submission = req.body.asJson.get
      val payload = (submission \ "payload").get.toString


      BlottrRepo.updateBlottrPayload(key, payload)

      NoContent
    }
  }

  def deleteBlottr(key: String) = CORSable(BlottrConfig().composerUrl) {
    APIAuthAction { req =>

      BlottrRepo.removeBlottr(key)

      NoContent
    }
  }

  def allowCORSAccess(methods: String, args: Any*) = CORSable(BlottrConfig().composerUrl) {

    Action { implicit req =>
      val requestedHeaders = req.headers("Access-Control-Request-Headers")
      NoContent.withHeaders("Access-Control-Allow-Methods" -> methods, "Access-Control-Allow-Headers" -> requestedHeaders)
    }
  }

}
