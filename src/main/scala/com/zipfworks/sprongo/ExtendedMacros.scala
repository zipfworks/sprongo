package com.zipfworks.sprongo

import org.joda.time.DateTime
import reactivemongo.bson.{BSONLong, BSONHandler}

trait ExtendedMacros {
  implicit object DateTimeBSONHandler extends BSONHandler[BSONLong, DateTime] {
    override def write(t: DateTime): BSONLong = BSONLong(t.getMillis)
    override def read(bson: BSONLong): DateTime = new DateTime(bson.value)
  }
}

object ExtendedMacros extends ExtendedMacros
