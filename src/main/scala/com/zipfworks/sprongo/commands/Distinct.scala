package com.zipfworks.sprongo.commands

import reactivemongo.bson.{BSONArray, BSONDocument}
import reactivemongo.core.commands.{CommandError, BSONCommandResultMaker, CommandResultMaker, Command}

case class Distinct(
  collectionName: String,
  field: String,
  query: Option[BSONDocument] = None) extends Command[BSONArray]{
  override val ResultMaker: CommandResultMaker[BSONArray] = Distinct

  override def makeDocuments: BSONDocument = BSONDocument(
    "distinct" -> collectionName,
    "key" -> field,
    "query" -> query
  )
}

object Distinct extends BSONCommandResultMaker[BSONArray]{
  override def apply(document: BSONDocument): Either[CommandError, BSONArray] = {
    CommandError.checkOk(document, Some("values")).toLeft(document.get("values") match {
      case None => BSONArray()
      case Some(d) => d.asInstanceOf[BSONArray]
    })
  }
}
