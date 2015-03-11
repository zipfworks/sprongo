package com.zipfworks.sprongo.dsl

import reactivemongo.api.ReadPreference
import reactivemongo.bson.{BSONDocument, BSONValue, Producer}

case class CountQuery[T](
  selector: T = BSONDocument(),
  readPref: ReadPreference = ReadPreference.Nearest(None)
){
  def readPref(rp: ReadPreference): CountQuery[T] = this.copy(readPref = rp)
}

trait CountDsl {

  class CountExpectsSel {
    def id(id: String): CountQuery[BSONDocument] = CountQuery(selector = BSONDocument("_id" -> id))
    def id(bval: BSONValue): CountQuery[BSONDocument] = CountQuery(selector = BSONDocument("_id" -> bval))
    def selector[T](sel: T): CountQuery[T] = CountQuery(selector = sel)
    def selector(sel: Producer[(String, BSONValue)]*): CountQuery[BSONDocument] = CountQuery(selector = BSONDocument(sel: _*))
  }

  def count: CountExpectsSel = new CountExpectsSel()

}

object CountDsl extends CountDsl
