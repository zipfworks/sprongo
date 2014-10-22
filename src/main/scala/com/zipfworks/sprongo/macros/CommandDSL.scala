package com.zipfworks.sprongo.macros

import reactivemongo.bson.{Producer, BSONValue, BSONDocument}

trait CommandDSL {

  case class CountCommand(s: BSONDocument)
  def count(s: BSONDocument = BSONDocument()) = CountCommand(s)
  def count(s: Producer[(String, BSONValue)]*) = CountCommand(BSONDocument(s: _*))

  case class DistinctCommand(field: String, selector: Option[BSONDocument] = None){
    def selector(s: BSONDocument) = this.copy(selector = Some(s))
    def selector(s: Producer[(String, BSONValue)]*) = this.copy(selector = Some(BSONDocument(s: _*)))
  }
  def distinct(field: String) = DistinctCommand(field)

}

object CommandDSL extends CommandDSL
