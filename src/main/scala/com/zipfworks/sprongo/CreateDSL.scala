package com.zipfworks.sprongo

import reactivemongo.api.bulk
import reactivemongo.bson.BSONDocument
import spray.json._

trait CreateDSL {

  def create: CreateExpectsDocument = new CreateExpectsDocument()

  case class CreateQuery(document: BSONDocument)
  case class CreateBulkQuery[T <: Model](ds: Seq[T], bulkSize: Int = bulk.MaxDocs, bulkByteSize: Int = bulk.MaxBulkSize){
    def bulkSize(i: Int): CreateBulkQuery[T] = this.copy(bulkSize = i)
    def bulkByteSize(i: Int): CreateBulkQuery[T] = this.copy(bulkByteSize = i)
  }

  class CreateExpectsDocument {

    def doc(d: BSONDocument) = CreateQuery(d)
    def model[T <: Model](m: T)(implicit f: RootJsonFormat[T]) =
      CreateQuery(JsonBsonConverter.jsObjToBdoc(m.toJson.asJsObject))

    def models[T <: Model](ms: Seq[T]) = CreateBulkQuery(ms)
  }

}
