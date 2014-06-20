package com.zipfworks.sprongo

import reactivemongo.bson.{BSONDocument, BSONValue, Producer}

trait DeleteDSL {

  def delete: DeleteExpectsSelector = new DeleteExpectsSelector()

  class DeleteExpectsSelector {

    def selector(s: BSONDocument)                    = DeleteQuery(s = s)
    def selector(s: Producer[(String, BSONValue)] *) = DeleteQuery(s = BSONDocument(s: _*))

    def id(id: String)    = DeleteQuery(s = BSONDocument("_id" -> id))
    def id(id: BSONValue) = DeleteQuery(s = BSONDocument("_id" -> id))

    def model[T <: Model](m: T) = DeleteModelQuery(m)
  }

  case class DeleteQuery(s: BSONDocument, firstMatchOnly: Boolean = false){
    def firstMatchOnly(b: Boolean): DeleteQuery = this.copy(firstMatchOnly = b)
  }

  case class DeleteModelQuery[T <: Model](m: T, firstMatchOnly: Boolean = false){
    def firstMatchOnly(b: Boolean): DeleteModelQuery[T] = this.copy(firstMatchOnly = b)
  }

}
