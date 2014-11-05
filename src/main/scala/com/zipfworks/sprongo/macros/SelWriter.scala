package com.zipfworks.sprongo.macros

import java.util.UUID
import reactivemongo.bson._
import Producer._

/** Allows you to pass in one thing and auto-generate a selector for it **/
trait SelWriter[B] {
  def write(t: B): BSONDocument
}

/** For use with models to generate a selector **/
trait ModelSelWriter {
  def write: BSONDocument
}

trait ObjectIDModel extends ModelSelWriter {
  def _id: BSONObjectID
  override def write = BSONDocument("_id" -> _id)
}

object SelWriters {

  implicit case object StringSelWriter extends SelWriter[String] {
    override def write(t: String): BSONDocument = BSONDocument("_id" -> BSONString(t))
  }

  implicit case object UUIDSelWriter extends SelWriter[UUID] {
    override def write(t: UUID): BSONDocument = BSONDocument("_id" -> BSONString(t.toString))
  }

  implicit case object ObjectIDWriter extends SelWriter[BSONObjectID] {
    override def write(t: BSONObjectID): BSONDocument = BSONDocument("_id" -> t)
  }

  implicit case object IdentityWriter extends SelWriter[BSONDocument] {
    override def write(t: BSONDocument): BSONDocument = t
  }

}
