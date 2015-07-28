package com.zipfworks.sprongo.commands

import reactivemongo.api.{BSONSerializationPack, SerializationPack}
import reactivemongo.api.commands._
import reactivemongo.bson.{BSONArray, BSONDocumentReader, BSONDocumentWriter, BSONDocument}


trait DistinctCommand[P <: SerializationPack] extends ImplicitCommandHelpers[P] {

  case class Distinct(key: String, query: BSONDocument)
    extends CollectionCommand
    with CommandWithPack[pack.type]
    with CommandWithResult[BSONArray]

}

object BSONDistinctCommand extends DistinctCommand[BSONSerializationPack.type] {
  val pack = BSONSerializationPack
  object Implicits {

    implicit object BSONWriter extends BSONDocumentWriter[ResolvedCollectionCommand[Distinct]] {
      override def write(t: ResolvedCollectionCommand[BSONDistinctCommand.Distinct]): BSONDocument = {
        BSONDocument("distinct" -> t.collection, "key" -> t.command.key, "query" -> t.command.query)
      }
    }

    implicit object BSONReader extends BSONDocumentReader[BSONArray] {
      override def read(bson: BSONDocument): BSONArray = {
        bson.get("values").get.asInstanceOf[BSONArray]
      }
    }
  }
}
