package com.zipfworks.sprongo

import reactivemongo.bson.BSONDocument
import spray.json._

trait CreateDSL {

  def create: CreateExpectsDocument = new CreateExpectsDocument()

  case class CreateQuery(document: BSONDocument)

  class CreateExpectsDocument {
    def doc(d: BSONDocument) = CreateQuery(d)
    def model[T <: Model](m: T)(implicit f: RootJsonFormat[T]) =
      CreateQuery(JsonBsonConverter.jsObjToBdoc(m.toJson.asJsObject))
  }

}
