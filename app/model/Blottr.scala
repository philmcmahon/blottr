package model

import java.util.concurrent.atomic.{AtomicReference, AtomicLong}


case class Blottr(
                   id: Long,
                   desc: String,
                   `type`: String,
                   payload: Option[String],
                   user: Option[String],
                   composerId: Option[String],
                   tags: List[String]) {

  def asJson = s"""{"id":${id}, "desc":"${desc}", "type":"${`type`}" ${payload.map(", payload:" + _).getOrElse("")}}"""
}

case class BlottrQuery(
                        user: Option[String],
                        composerId: Option[String],
                        tags: Option[List[String]])

object BlottrRepo {
  val idSeq = new AtomicLong(1)

  val blottrStore = new AtomicReference[List[Blottr]](Nil)

  def findBlottrs(q: BlottrQuery) = {
    val blottrs = blottrStore.get()

    val byUser = q.user.map{ u => blottrs.filter(_.user == Some(u)) }.getOrElse(Nil)

    val byComposer = q.composerId.map{ c =>
      val cb = blottrs.filter(_.composerId == Some(c))

      if(cb.isEmpty) {
        List(getOrInitialiseForComposer(c))
      } else {
        cb
      }
    }.getOrElse(Nil)

    val byTag = q.tags.map{ ts =>
      blottrs.filter(_.tags.intersect(ts).size > 0)
    }.getOrElse(Nil)

   val all =  byComposer ::: byUser ::: byTag

    all.distinct
  }

  def getOrInitialiseForComposer(composerId: String) = {
    val blottrs = blottrStore.get()
    val existing = blottrs.filter(_.composerId == Some(composerId)).headOption
    existing getOrElse {
      val id = idSeq.incrementAndGet()
      val blottr = Blottr(id, "Content blottr", "content", None, None, Some(composerId), Nil)
      blottrStore.set(blottr :: blottrStore.get)
      blottr
    }
  }

  def addUserBlottr(user: String) = {
    val blottrs = blottrStore.get()
    val existing = blottrs.filter(_.user == Some(user)).headOption
    existing getOrElse {
      val id = idSeq.incrementAndGet()
      val blottr = Blottr(id, "Personal blottr", "user", None, Some(user), None, Nil)
      blottrStore.set(blottr :: blottrStore.get)
      blottr
    }
  }

  def updateBlottrPayload(id: Long, payload: String) = {
    val blottr = lookupById(id)
    blottr.foreach { b =>
      blottrStore.set(b.copy(payload = Some(payload)) :: blottrStore.get.filterNot(_.id == id))
    }
  }

  def removeBlottr(id: Long) {
    blottrStore.set(blottrStore.get.filterNot(_.id == id))
  }

  def spawnTagBlottr(id: Long, tag: String, desc: String) {
    val blottr = lookupById(id)
    blottr.foreach { b =>

      val spawnedBlottr = b.copy(
        id = idSeq.incrementAndGet(),
        desc = s"Tag $desc blottr",
        `type` = "tag",
        user = None,
        composerId = None,
        tags = tag :: b.tags
      )

      blottrStore.set(spawnedBlottr :: blottrStore.get)
    }
  }

  private def lookupById(id: Long) = {
    blottrStore.get().filter(_.id == id).headOption
  }
}
