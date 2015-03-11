package com.zipfworks.sprongo

import reactivemongo.bson.{Producer, BSONValue, BSONDocument}
import reactivemongo.core.commands.GetLastError

case class UpdateQuery(
  selector: BSONDocument,
  update: BSONDocument,
  upsert: Boolean = false,
  multi: Boolean = false,
  writeConcern: GetLastError = GetLastError()
){
  def upsert(b: Boolean): UpdateQuery = this.copy(upsert = b)
  def multi(b: Boolean): UpdateQuery = this.copy(multi = b)
  def writeConcern(gle: GetLastError): UpdateQuery = this.copy(writeConcern = gle)
}

trait UpdateDSL {

  class UpdateExpectsSelector {
    def selector(s: BSONDocument)                   = new UpdateExpectsUpdateDef(s)
    def selector(s: Producer[(String, BSONValue)]*) = new UpdateExpectsUpdateDef(BSONDocument(s: _*))

    def id(id: String)    = new UpdateExpectsUpdateDef(BSONDocument("_id" -> id))
    def id(id: BSONValue) = new UpdateExpectsUpdateDef(BSONDocument("_id" -> id))
  }

  class UpdateExpectsUpdateDef(s: BSONDocument) {
    def update(u: UpdateOp*) = {
      val ups = u.foldLeft(BSONDocument())((doc, ups) => doc.add(ups.build))
      UpdateQuery(s, ups)
    }
  }

  def update = new UpdateExpectsSelector()

}
