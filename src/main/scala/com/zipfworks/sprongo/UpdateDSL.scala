package com.zipfworks.sprongo

import reactivemongo.bson.{BSONInteger, Producer, BSONValue, BSONDocument}
import spray.json._

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

  case class UpdateModelQuery[T](m: T, upsert: Boolean = false, multi: Boolean = false){
    def upsert(b: Boolean): UpdateModelQuery[T] = this.copy(upsert = b)
    def multi(b: Boolean): UpdateModelQuery[T] = this.copy(multi = b)
  }

  class UpdateExpectsSelector {

    def selector(s: BSONDocument)                    = new UpdateExpectsUpdateDef(s)
    def selector(s: Producer[(String, BSONValue)] *) = new UpdateExpectsUpdateDef(BSONDocument(s: _*))

    def model[T <: Model](m: T) = UpdateModelQuery[T](m)

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
  case class set(fieldVal: BSONDocument) extends UpdateOperation {
    override def build: BSONDocument = {
      BSONDocument("$set" -> fieldVal)
    }
  }
  object set {

    def apply(fieldVal: Producer[(String, BSONValue)]*)
             (implicit d: DummyImplicit): set = {
      set(BSONDocument(fieldVal: _*))
    }

    def apply(fieldVal: (String, BSONValue)*)
             (implicit d: DummyImplicit, d1: DummyImplicit): set = {
      set(BSONDocument(fieldVal))
    }

    def apply(fieldVal: (String, JsValue)*)
             (implicit d: DummyImplicit, d1: DummyImplicit, d2: DummyImplicit): set = {
      set(BSONDocument(fieldVal.toMap.mapValues(JsonBsonConverter.jsValueToBsonVal)))
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

  /** http://docs.mongodb.org/manual/reference/operator/update/push/#up._S_push **/
  case class push(doc: BSONDocument) extends UpdateOperation {
    override def build: BSONDocument = BSONDocument("$push" -> doc)
  }

  object push {

    def apply(fieldVal: Producer[(String, BSONValue)]*): push = push(BSONDocument(fieldVal: _*))

    def apply[T](field: String, v: T)(implicit w: JsonWriter[T]): push =
      push(BSONDocument(field -> JsonBsonConverter.jsValueToBsonVal(v.toJson)))

    def apply(fieldVal: (String, JsValue)*)(implicit d: DummyImplicit): push =
      push(BSONDocument(fieldVal.toMap.mapValues(JsonBsonConverter.jsValueToBsonVal)))
  }

  /** http://docs.mongodb.org/manual/reference/operator/update/pull/#up._S_pull **/
  case class pull(doc: BSONDocument) extends UpdateOperation {
    override def build: BSONDocument = BSONDocument("$pull" -> doc)
  }

  object pull {

    def apply(field: String, criteria: BSONValue):pull = pull(BSONDocument(field -> criteria))

    def apply[T](field: String, criteria: T)(implicit w: JsonWriter[T]): pull =
      apply(field, JsonBsonConverter.jsValueToBsonVal(criteria.toJson))
  }

}
