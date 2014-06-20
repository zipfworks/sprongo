package com.zipfworks.sprongo

import reactivemongo.bson.{BSONInteger, Producer, BSONValue, BSONDocument}
import spray.json._

//in case you just want the update dsl piece
object UpdateDSL extends UpdateDSL

trait UpdateDSL {

  def update = new UpdateExpectsSelector()

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

    def selector(s: BSONDocument)                    = new UpdateExpectsUpdateDef(s)
    def selector(s: Producer[(String, BSONValue)] *) = new UpdateExpectsUpdateDef(BSONDocument(s: _*))

    def model[T <: Model](m: T)(implicit writer: JsonWriter[T]) = UpdateQuery(
      selector = BSONDocument("_id" -> m.id),
      update = JsonBsonConverter.jsObjToBdoc(m.toJson.asJsObject)
    )

    def id(id: String)    = new UpdateExpectsUpdateDef(BSONDocument("_id" -> id))
    def id(id: BSONValue) = new UpdateExpectsUpdateDef(BSONDocument("_id" -> id))
  }

  class UpdateExpectsUpdateDef(s: BSONDocument) {
    def update(u: UpdateOperation*) = {
      val ups = u.foldLeft(BSONDocument())((doc, ups) => doc.add(ups.build))
      UpdateQuery(s, ups)
    }
  }

  /**********************************************************************************
   * Update Operations
   *********************************************************************************/
  trait UpdateOperation {
    def build: BSONDocument
  }

  /** http://docs.mongodb.org/manual/reference/operator/update/set/ **/
  case class set(fieldVal: Producer[(String, BSONValue)]*) extends UpdateOperation {
    override def build: BSONDocument = {
      BSONDocument("$set" -> BSONDocument(fieldVal: _*))
    }
  }

  /** http://docs.mongodb.org/manual/reference/operator/update/unset/ **/
  case class unset(fields: String*) extends UpdateOperation {
    override def build: BSONDocument = {
      val unsets = fields.map(s => s -> BSONInteger(1))
      BSONDocument("$unset" -> BSONDocument(unsets))
    }
  }

  /** http://docs.mongodb.org/manual/reference/operator/update/inc/ **/
  case class inc(fieldVal: Producer[(String, BSONValue)]*) extends UpdateOperation {
    override def build: BSONDocument = {
      BSONDocument("$inc" -> BSONDocument(fieldVal: _*))
    }
  }

}
