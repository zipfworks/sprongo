package com.zipfworks.sprongo

import reactivemongo.bson.{BSONDateTime, BSONObjectID}
import spray.json._
import org.joda.time.DateTime
import reactivemongo.api.SortOrder

trait ExtendedJsonProtocol extends DefaultJsonProtocol {

  implicit object DateTimeJsonFormat extends RootJsonFormat[DateTime] {
    def write(dt: DateTime) = JsNumber(dt.getMillis)
    def read(value: JsValue) = new DateTime(value.asInstanceOf[JsNumber].value.toLong)
  }

  implicit object BSONDateTimeJsonFormat extends RootJsonFormat[BSONDateTime] {
    def write(dt: BSONDateTime) = JsString(new DateTime(dt.value).toString())
    def read(value: JsValue) = BSONDateTime(DateTime.parse(value.asInstanceOf[JsString].value).getMillis)
  }

  implicit object SortOrderJsonFormat extends RootJsonFormat[SortOrder] {
    def write(so: SortOrder) = so match {
      case asc: SortOrder.Ascending.type => JsNumber(1)
      case desc: SortOrder.Descending.type => JsNumber(-1)
    }
    def read(value: JsValue) = value.asInstanceOf[JsNumber].value.toInt match {
      case 1 => SortOrder.Ascending
      case -1 => SortOrder.Descending
    }
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
