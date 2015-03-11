package com.zipfworks.sprongo.dsl

import reactivemongo.api.ReadPreference
import reactivemongo.bson.{BSONDocument, BSONValue, Producer}

case class DistinctQuery[T](
  field: String,
  selector: T = BSONDocument(),
  readPref: ReadPreference = ReadPreference.Nearest(None)
){

  def selector[S](sel: S): DistinctQuery[S] = {
    this.copy[S](selector = sel)
  }

  def selector(sel: Producer[(String, BSONValue)]*): DistinctQuery[BSONDocument] = {
    this.copy(selector = BSONDocument(sel: _*))
  }

  def readPref(rp: ReadPreference): DistinctQuery[T] = {
    this.copy(readPref = rp)
  }

}

trait DistictDsl {

  class DistinctExpectsField {
    def field(f: String): DistinctQuery[BSONDocument] = DistinctQuery[BSONDocument](field = f)
  }

  def distinct: DistinctExpectsField = new DistinctExpectsField()

}

object DistictDsl extends DistictDsl
