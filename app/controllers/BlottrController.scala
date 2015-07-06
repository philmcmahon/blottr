package controllers

import model.{BlottrRepo, BlottrQuery}
import play.api.mvc.{Action, Controller}


object BlottrController extends Controller {

  def hello = Action { Ok("Hello from blottr") }

  def search = Action { req =>

    val query = BlottrQuery(
      user = req.getQueryString("user"),
      composerId = req.getQueryString("composerId"),
      tags = req.getQueryString("tags").map(_.split(",").toList)
    )

    val blottrs = BlottrRepo.findBlottrs(query).map(_.asJson).mkString("[", ",", "]")


    Ok(blottrs).as(JSON)
  }

}
