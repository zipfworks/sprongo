package com.zipfworks.sprongo

import reactivemongo.bson._
import reactivemongo.core.commands.GetLastError

case class DeleteQuery(
  s: BSONDocument,
  firstMatchOnly: Boolean = false,
  writeConcern: GetLastError = GetLastError()
){
  def firstMatchOnly(b: Boolean): DeleteQuery = this.copy(firstMatchOnly = b)
  def writeConcern(gle: GetLastError): DeleteQuery = this.copy(writeConcern = gle)
}

trait DeleteDSL {

  class DeleteExpectsSelector {

    def selector(s: BSONDocument)                    = DeleteQuery(s = s)
    def selector(s: Producer[(String, BSONValue)]*) = DeleteQuery(s = BSONDocument(s: _*))

    def id(id: String): DeleteQuery    = DeleteQuery(s = BSONDocument("_id" -> id))
    def id(id: BSONValue): DeleteQuery = DeleteQuery(s = BSONDocument("_id" -> id))

  }

  def delete: DeleteExpectsSelector = new DeleteExpectsSelector()

}
