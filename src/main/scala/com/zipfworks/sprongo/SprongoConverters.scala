package com.zipfworks.sprongo

import org.joda.time.DateTime
import reactivemongo.bson._

object SprongoConverters {

  implicit object IntTupleHandler extends BSONWriter[(Int, Int), BSONArray] with BSONReader[BSONArray, (Int, Int)] {
    override def write(t: (Int, Int)): BSONArray = BSONArray(BSONInteger(t._1), BSONInteger(t._2))
    override def read(bson: BSONArray): (Int, Int) = (bson.getAs[BSONInteger](0).get.value, bson.getAs[BSONInteger](1).get.value)
  }

  implicit object JodaDateTimeHandler extends BSONWriter[DateTime, BSONLong] with BSONReader[BSONLong, DateTime] {
    override def write(t: DateTime): BSONLong = BSONLong(t.getMillis)
    override def read(bson: BSONLong): DateTime = new DateTime(bson.value)
  }

}

