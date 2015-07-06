package controllers

import play.api.mvc.{Action, Controller}


object BlottrController extends Controller {

  def hello = Action { Ok("Hello from blottr") }

}
