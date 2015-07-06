package model

import java.util.concurrent.atomic.{AtomicReference, AtomicLong}


case class Blottr(
                   id: Long,
                   payload: Option[String],
                   user: Option[String],
                   composerId: Option[String],
                   tags: List[String]) {

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
    val byComposer = q.composerId.map{ c => blottrs.filter(_.composerId == Some(c)) }.getOrElse(Nil)
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
      val blottr = Blottr(id, None, None, Some(composerId), Nil)
      blottrStore.set(blottr :: blottrStore.get)
      blottr
    }
  }

  def addUserBlottr(user: String) = {
    val blottrs = blottrStore.get()
    val existing = blottrs.filter(_.user == Some(user)).headOption
    existing getOrElse {
      val id = idSeq.incrementAndGet()
      val blottr = Blottr(id, Some(user), None, None, Nil)
      blottrStore.set(blottr :: blottrStore.get)
      blottr
    }
  }

  def removeBlottr(id: Long) {
    blottrStore.set(blottrStore.get.filterNot(_.id == id))
  }

  def tagBlottr(id: Long, tag: String) {
    val blottr = lookupById(id)
    blottr.foreach { b =>
      blottrStore.set(b.copy(tags = tag :: b.tags) :: blottrStore.get.filterNot(_.id == id))
    }
  }

  private def lookupById(id: Long) = {
    blottrStore.get().filter(_.id == id).headOption
  }
}
