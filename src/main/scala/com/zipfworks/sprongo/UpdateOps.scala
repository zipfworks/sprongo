package com.zipfworks.sprongo

import reactivemongo.bson._

trait UpdateOp {
  def build: BSONDocument
}

object UpdateOps {
  /** http://docs.mongodb.org/manual/reference/operator/update/set/ **/
  case class $set(fieldVal: BSONDocument) extends UpdateOp {
    def build: BSONDocument = BSONDocument("$set" -> fieldVal)
  }

  object $set {
    def apply(fieldVal: Producer[(String, BSONValue)]*): $set = $set(BSONDocument(fieldVal: _*))
    def apply(field: String, value: BSONValue): $set = $set(BSONDocument(field -> value))
  }


  /** http://docs.mongodb.org/manual/reference/operator/update/unset/ **/
  case class $unset(fields: String*) extends UpdateOp {
    def build: BSONDocument = {
      val unsets = fields.map(s => s -> BSONInteger(1))
      BSONDocument("$unset" -> BSONDocument(unsets))
    }
  }


  /** http://docs.mongodb.org/manual/reference/operator/update/inc/ **/
  case class $inc(fieldVal: BSONDocument) extends UpdateOp {
    def build: BSONDocument = BSONDocument("$inc" -> fieldVal)
  }
  object $inc {
    def apply(fieldVal: Producer[(String, BSONValue)]*): $inc = $inc(BSONDocument(fieldVal: _*))
    def apply(field: String, amount: Int): $inc = $inc(BSONDocument(field -> amount))
  }


  /** http://docs.mongodb.org/manual/reference/operator/update/push/#up._S_push **/
  case class $push(doc: BSONDocument) extends UpdateOp {
    def build: BSONDocument = BSONDocument("$push" -> doc)
  }
  object $push {
    def apply(fieldVal: Producer[(String, BSONValue)]*): $push = $push(BSONDocument(fieldVal: _*))
    def apply(field: String, value: BSONValue): $push = $push(BSONDocument(field -> value))
  }


  /** http://docs.mongodb.org/manual/reference/operator/update/pull/#up._S_pull **/
  case class $pull(doc: BSONDocument) extends UpdateOp {
    override def build: BSONDocument = BSONDocument("$pull" -> doc)
  }
  object $pull {
    def apply(field: String, criteria: BSONValue): $pull =
      $pull(BSONDocument(field -> criteria))

    def apply[T](field: String, criteria: Producer[(String, BSONValue)]*): $pull =
      $pull(BSONDocument(field -> BSONDocument(criteria: _*)))
  }


  /** http://docs.mongodb.org/manual/reference/operator/update/addToSet/ **/
  case class $addToSet(field: String, value: BSONValue) extends UpdateOp {
    override def build: BSONDocument = BSONDocument("$addToSet" -> BSONDocument(field -> value))
  }


  /** http://docs.mongodb.org/manual/reference/operator/update/pop/ **/
  sealed trait PopPosition {def value: Int}
  case object First extends PopPosition {val value = -1}
  case object Last extends PopPosition {val value = 1}

  case class $pop(field: String, pos: PopPosition) extends UpdateOp {
    override def build: BSONDocument = BSONDocument("$pop" -> BSONDocument(field -> pos.value))
  }

}

