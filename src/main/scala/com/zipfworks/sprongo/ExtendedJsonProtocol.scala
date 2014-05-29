package com.zipfworks.sprongo

import spray.json._
import org.joda.time.DateTime
import reactivemongo.api.SortOrder

trait ExtendedJsonProtocol extends DefaultJsonProtocol {

  implicit object DateTimeJsonFormat extends RootJsonFormat[DateTime] {
    def write(dt: DateTime) = JsNumber(dt.getMillis)
    def read(value: JsValue) = new DateTime(value.asInstanceOf[JsNumber].value.toLong)
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
}

object ExtendedJsonProtocol extends ExtendedJsonProtocol
