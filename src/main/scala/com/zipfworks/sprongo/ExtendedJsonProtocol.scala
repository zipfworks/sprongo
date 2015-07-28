package com.zipfworks.sprongo

import reactivemongo.bson.{BSONDateTime, BSONObjectID}
import spray.json._
import org.joda.time.DateTime

trait ExtendedJsonProtocol extends DefaultJsonProtocol {

  implicit object DateTimeJsonFormat extends RootJsonFormat[DateTime] {
    def write(dt: DateTime) = JsNumber(dt.getMillis)
    def read(value: JsValue) = new DateTime(value.asInstanceOf[JsNumber].value.toLong)
  }

  implicit object BSONDateTimeJsonFormat extends RootJsonFormat[BSONDateTime] {
    def write(dt: BSONDateTime) = JsString(new DateTime(dt.value).toString())
    def read(value: JsValue) = BSONDateTime(DateTime.parse(value.asInstanceOf[JsString].value).getMillis)
  }

  implicit object BSONObjectIDJsonFormat extends JsonFormat[BSONObjectID] {
    def write(id: BSONObjectID) = JsString(id.stringify)
    def read(value: JsValue) = value match {
      case JsString(x) => BSONObjectID(x)
      case x => deserializationError("Expected BSONObjectID as JsString, but got " + x)
    }
  }
}

object ExtendedJsonProtocol extends ExtendedJsonProtocol
