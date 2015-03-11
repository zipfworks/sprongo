package com.zipfworks.sprongo

import org.joda.time.DateTime
import reactivemongo.bson.{BSONArray, BSONDocument, BSONValue}

trait SprongoSels {

  def $in(values: Seq[BSONValue]): BSONDocument = BSONDocument("$in" -> BSONArray(values))
  def $nin(values: Seq[BSONValue]): BSONDocument = BSONDocument("$nin" -> BSONArray(values))

  def $ne(value: BSONValue): BSONDocument = BSONDocument("$ne" -> value)

  def $gt(long: Long): BSONDocument = BSONDocument("$gt" -> long)
  def $gt(dt: DateTime): BSONDocument = BSONDocument("$gt" -> dt.getMillis)
  def $gte(long: Long): BSONDocument = BSONDocument("$gte" -> long)
  def $gte(dt: DateTime): BSONDocument = BSONDocument("$gte" -> dt.getMillis)

  def $lt(long: Long): BSONDocument = BSONDocument("$lt" -> long)
  def $lt(dt: DateTime): BSONDocument = BSONDocument("$lt" -> dt.getMillis)
  def $lte(long: Long): BSONDocument = BSONDocument("$lte" -> long)
  def $lte(dt: DateTime): BSONDocument = BSONDocument("$lte" -> dt.getMillis)
}

object SprongoSels extends SprongoSels
