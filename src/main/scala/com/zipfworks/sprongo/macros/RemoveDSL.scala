package com.zipfworks.sprongo.macros

import reactivemongo.bson.{BSONWriter, Producer, BSONValue, BSONDocument}
import reactivemongo.core.commands.GetLastError

trait RemoveDSL {

  case class RemoveQuery(
    selector: BSONDocument,
    writeConcern: GetLastError = GetLastError(),
    multi: Boolean = false
  ){
    def writeConcern(wr: GetLastError): RemoveQuery = this.copy(writeConcern = wr)
    def multi(b: Boolean): RemoveQuery = this.copy(multi = b)
  }

  //remove("some-id-goes-here"); remove(BSONObjectId("some-other-id"))
  def remove[S](selector: S)(implicit selWriter: SelWriter[S]): RemoveQuery =
    RemoveQuery(selector = selWriter.write(selector))

  //remove(MyCaseClassModel("cool!"))
  def remove[A <: ModelSelWriter](selector: A): RemoveQuery =
    RemoveQuery(selector = selector.write)

  //remove("key" -> "value", "key2" -> "value2")
  def remove(selector: Producer[(String, BSONValue)]*): RemoveQuery =
    RemoveQuery(selector = BSONDocument(selector: _*))

  //remove("key" -> "value")
  def remove[B](selector: (String, B))(implicit writer: BSONWriter[B, _ <: BSONValue]): RemoveQuery =
    RemoveQuery(selector = BSONDocument(Producer.nameValue2Producer(selector)))
}

object RemoveDSL extends RemoveDSL
