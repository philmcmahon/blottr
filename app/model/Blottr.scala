package model

import com.amazonaws.services.dynamodbv2.document.Item
import services.AWS.Dynamo


case class Blottr(
                   key: String,
                   desc: String,
                   `type`: String,
                   payload: Option[String]) {

  def asJson = s"""{"key":"${key}", "desc":"${desc}", "type":"${`type`}" ${payload.map(""", "payload":""" + _).getOrElse("")}}"""

  def asDynamoItem = {
    val item = new Item()
      .withString("key", key)
      .withString("desc", desc)
      .withString("type", `type`)

    payload.map(item.withString("payload", _)).getOrElse(item)
  }
}

object Blottr {
  def apply(item: Item): Blottr = Blottr(
    key = item.getString("key"),
    desc = item.getString("desc"),
    `type` = item.getString("type"),
    payload = Option(item.getString("payload"))
  )
}

case class BlottrQuery(user: Option[String], composerId: Option[String])

object BlottrRepo {

  val tableName = "blottr"
  lazy val blottrTable = Dynamo.getTable(tableName)


  def findBlottrs(q: BlottrQuery) = {

    val composerBlottr = q.composerId.map("composer:" + _).map{ key =>
      lookupByKey(key).getOrElse(createComposerBlottr(key))
    }
    val userBlottr = q.user.map("user:" + _).flatMap(lookupByKey(_))

    Seq(composerBlottr, userBlottr).flatten

  }

  def addUserBlottr(user: String) = {
    val key = s"user:$user"
    lookupByKey(key).getOrElse(createUserBlottr(key))
  }

  def updateBlottrPayload(key: String, payload: String) = {
    val blottr = lookupByKey(key)
    blottr.foreach { b =>
      saveBlottr(b.copy(payload = Some(payload)))
    }
  }

  def removeBlottr(key: String) = {
    blottrTable.deleteItem("key", key)
  }

  private def createComposerBlottr(key: String) = {
    val blottr = Blottr(key, "Content blottr", "content", None)
    saveBlottr(blottr)

    blottr
  }
  private def createUserBlottr(key: String) = {
    val blottr = Blottr(key, "Personal blottr", "user", None)
    saveBlottr(blottr)

    blottr
  }

  private def lookupByKey(key: String) = {
    Option(blottrTable.getItem("key", key)).map(Blottr(_))
  }

  private def saveBlottr(blottr: Blottr) = {
    blottrTable.putItem(blottr.asDynamoItem)
  }


}
