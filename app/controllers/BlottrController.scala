package controllers

import model.{BlottrRepo, BlottrQuery}
import play.api.mvc._
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

object BlottrController extends Controller {

  def hello = Action {
    Ok("Hello from blottr")
  }

  def search = CORSable("https://composer.local.dev-gutools.co.uk") {
    Action { req =>

      val query = BlottrQuery(
        user = req.getQueryString("user"),
        composerId = req.getQueryString("composerId"),
        tags = req.getQueryString("tags").map(_.split(",").toList)
      )

      val blottrs = BlottrRepo.findBlottrs(query).map(_.asJson).mkString("[", ",", "]")


      Ok(blottrs).as(JSON)
    }
  }

  def addPersonalBlottr = CORSable("https://composer.local.dev-gutools.co.uk") {
    Action { req =>

      val submission = req.body.asJson.get
      val user = (submission \ "user").get.as[String]

      BlottrRepo.addUserBlottr(user)

      NoContent
    }
  }

  def putPayload(id: Long) = CORSable("https://composer.local.dev-gutools.co.uk") {
    Action { req =>

      val submission = req.body.asJson.get
      val payload = (submission \ "payload").get.toString


      BlottrRepo.updateBlottrPayload(id, payload)

      NoContent
    }
  }

  def deleteBlottr(id: Long) = CORSable("https://composer.local.dev-gutools.co.uk") {
    Action { req =>

      BlottrRepo.removeBlottr(id)

      NoContent
    }
  }

  def allowCORSAccess(methods: String, args: Any*) = CORSable("https://composer.local.dev-gutools.co.uk") {

    Action { implicit req =>
      val requestedHeaders = req.headers("Access-Control-Request-Headers")
      NoContent.withHeaders("Access-Control-Allow-Methods" -> methods, "Access-Control-Allow-Headers" -> requestedHeaders)
    }
  }

}
