package com.zipfworks.sprongo.macros

import reactivemongo.bson._
import reactivemongo.core.commands.GetLastError

trait UpdateDSL {

  case class UpdateQuery(
    selector: BSONDocument,
    update: BSONDocument,
    writeConcern: GetLastError = GetLastError(),
    upsert: Boolean = false,
    multi: Boolean = false
  ){
    def writeConcern(wr: GetLastError): UpdateQuery = this.copy(writeConcern = wr)
    def upsert(b: Boolean): UpdateQuery = this.copy(upsert = b)
    def multi(b: Boolean): UpdateQuery = this.copy(multi = b)
  }

  /** Intermediary Step to assign updates **/
  class UpdateExpectsUpdateOps(selector: BSONDocument){
    def ops(ops: UpdateOps*): UpdateQuery =
      UpdateQuery(selector, BSONDocument(ops.map(_.build)))

    def ops(update: BSONDocument): UpdateQuery =
      UpdateQuery(selector, update)
  }

  trait UpdateOps {
    def build: (String, BSONValue)
  }

  /** http://docs.mongodb.org/manual/reference/operator/update/set/ **/
  case class $set[T](field: String, value: T)(implicit writer: BSONWriter[T, _ <: BSONValue]) extends UpdateOps {
    override def build: (String, BSONValue) = "$set" -> BSONDocument(Producer.nameValue2Producer(field -> value))
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
  case class $push[S](field: String, value: S)(implicit writer: BSONWriter[S, _ <: BSONValue]) extends UpdateOps {
    override def build: (String, BSONValue) = "$push" -> BSONDocument(Producer.nameValue2Producer(field -> value))
  }

  /** http://docs.mongodb.org/manual/reference/operator/update/pull/#up._S_pull **/
  case class $pull[S](field: String, value: S)(implicit writer: BSONWriter[S, _ <: BSONValue]) extends UpdateOps {
    override def build: (String, BSONValue) = "$pull" -> BSONDocument(Producer.nameValue2Producer(field -> value))
  }

  /** http://docs.mongodb.org/manual/reference/operator/update/addToSet/ **/
  case class $addToSet[S](field: String, value: S)(implicit writer: BSONWriter[S, _ <: BSONValue]) extends UpdateOps {
    override def build: (String, BSONValue) = "$addToSet" -> BSONDocument(Producer.nameValue2Producer(field -> value))
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
  //update("some-id"); update(BSONObjectId("some-id"))
  def update[S](selector: S)(implicit selWriter: SelWriter[S]) =
    new UpdateExpectsUpdateOps(selWriter.write(selector))

  //update(MyCaseClassModel("cool!"))
  def update[A <: ModelSelWriter](selector: A) =
    new UpdateExpectsUpdateOps(selector = selector.write)

  //update("key" -> "value", "key2" -> "value2")
  def update(selector: Producer[(String, BSONValue)]*) =
    new UpdateExpectsUpdateOps(selector = BSONDocument(selector: _*))

  //update("key" -> "value")
  def update[B](selector: (String, B))(implicit writer: BSONWriter[B, _ <: BSONValue]) =
    new UpdateExpectsUpdateOps(selector = BSONDocument(Producer.nameValue2Producer(selector)))
}

object UpdateDSL extends UpdateDSL
