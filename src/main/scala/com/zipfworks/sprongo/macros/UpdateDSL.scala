package com.zipfworks.sprongo.macros

import reactivemongo.bson._
import reactivemongo.core.commands.GetLastError

trait UpdateDSL {

  case class UpdateQuery[S](
    selector: S,
    update: BSONDocument,
    writeConcern: GetLastError = GetLastError(),
    upsert: Boolean = false,
    multi: Boolean = false
  ){
    def writeConcern(wr: GetLastError): UpdateQuery[S] = this.copy(writeConcern = wr)
    def upsert(b: Boolean): UpdateQuery[S] = this.copy(upsert = b)
    def multi(b: Boolean): UpdateQuery[S] = this.copy(multi = b)
  }

  /** Intermediary Step to assign updates **/
  class UpdateExpectsUpdateOps[S](selector: S){
    def ops(ops: UpdateOps*): UpdateQuery[S] =
      UpdateQuery(selector, BSONDocument(ops.map(_.build)))

    def ops(update: BSONDocument): UpdateQuery[S] =
      UpdateQuery(selector, update)
  }

  trait UpdateOps {
    def build: (String, BSONValue)
  }

  /** http://docs.mongodb.org/manual/reference/operator/update/set/ **/
  case class $set[T](field: String, value: T)(implicit writer: BSONWriter[T, BSONValue]) extends UpdateOps {
    override def build: (String, BSONValue) = "$set" -> BSONDocument(field -> writer.write(value))
  }

  /** http://docs.mongodb.org/manual/reference/operator/update/unset/ **/
  case class $unset(fields: String*) extends UpdateOps {
    override def build: (String, BSONValue) = "$unset" -> BSONDocument(fields.map(_ -> BSONInteger(1)))
  }

  /** http://docs.mongodb.org/manual/reference/operator/update/inc/ **/
  case class $inc(field: String, amount: Int) extends UpdateOps {
    override def build: (String, BSONValue) =  "$inc" -> BSONDocument(field -> amount)
  }

  /** http://docs.mongodb.org/manual/reference/operator/update/push/#up._S_push **/
  case class $push[S](field: String, value: S)(implicit writer: BSONWriter[S, BSONValue]) extends UpdateOps {
    override def build: (String, BSONValue) = "$push" -> BSONDocument(field -> writer.write(value))
  }

  /** http://docs.mongodb.org/manual/reference/operator/update/pull/#up._S_pull **/
  case class $pull[S](field: String, value: S)(implicit writer: BSONWriter[S, BSONValue]) extends UpdateOps {
    override def build: (String, BSONValue) = "$pull" -> BSONDocument(field -> writer.write(value))
  }

  /** http://docs.mongodb.org/manual/reference/operator/update/addToSet/ **/
  case class $addToSet[S](field: String, value: S)(implicit writer: BSONWriter[S, BSONValue]) extends UpdateOps {
    override def build: (String, BSONValue) = "$addToSet" -> BSONDocument(field -> writer.write(value))
  }

  /** http://docs.mongodb.org/manual/reference/operator/update/pop/ **/
  sealed trait PopPosition { def value: Int }
  object PopPositions {
    case object First extends PopPosition { override val value = -1 }
    case object Last  extends PopPosition { override val value = 1 }
  }
  case class $pop(field: String, pos: PopPosition) extends UpdateOps {
    override def build: (String, BSONValue) = "$pop" -> BSONDocument(field -> pos.value)
  }

  /** DSL **/
  def update[S](selector: S) = new UpdateExpectsUpdateOps[S](selector)

}

object UpdateDSL extends UpdateDSL
