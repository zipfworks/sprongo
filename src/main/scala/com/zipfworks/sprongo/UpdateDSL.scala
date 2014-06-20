package com.zipfworks.sprongo

import reactivemongo.bson.{BSONInteger, Producer, BSONValue, BSONDocument}

trait UpdateOperation {
  def build: BSONDocument
}

case class Set(fieldVal: Producer[(String, BSONValue)]*) extends UpdateOperation {
  override def build: BSONDocument = {
    BSONDocument("$set" -> BSONDocument(fieldVal: _*))
  }
}

case class Unset(fields: String*) extends UpdateOperation {
  override def build: BSONDocument = {
    val unsets = fields.map(s => s -> BSONInteger(1))
    BSONDocument("$unset" -> BSONDocument(unsets))
  }
}

case class Inc(fieldVal: Producer[(String, BSONValue)]*) extends UpdateOperation {
  override def build: BSONDocument = {
    BSONDocument("$inc" -> BSONDocument(fieldVal: _*))
  }
}


case class UpdateQuery(
  selector: BSONDocument,
  update: BSONDocument,
  upsert: Boolean = false,
  multi: Boolean = false
){
  def upsert(b: Boolean): UpdateQuery = this.copy(upsert = b)

  def multi(b: Boolean): UpdateQuery = this.copy(multi = b)
}

class UpdateExpectsSelector {
  def selector(s: BSONDocument) = new UpdateExpectsUpdateDef(s)
}

class UpdateExpectsUpdateDef(s: BSONDocument) {
  def update(u: UpdateOperation*) = {
    val ups = u.foldLeft(BSONDocument())((doc, ups) => doc.add(ups.build))
    UpdateQuery(s, ups)
  }
}

object UpdateDSL {
  def update = new UpdateExpectsSelector()
}
