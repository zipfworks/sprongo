package com.zipfworks.sprongo.macros

import java.util.UUID

import org.joda.time.DateTime
import reactivemongo.bson.{BSONString, BSONHandler, BSONDateTime}

trait ExtendedMacroHandlers {

  implicit object BSONDateTimeHandler extends BSONHandler[BSONDateTime, DateTime] {
    override def write(t: DateTime): BSONDateTime = BSONDateTime(t.getMillis)
    override def read(bson: BSONDateTime): DateTime = new DateTime(bson.value)
  }

  implicit object BSONJavaUUIDHandler extends BSONHandler[BSONString, UUID] {
    override def write(t: UUID): BSONString = BSONString(t.toString)
    override def read(bson: BSONString): UUID = UUID.fromString(bson.value)
  }

}

object ExtendedMacroHandlers extends ExtendedMacroHandlers
