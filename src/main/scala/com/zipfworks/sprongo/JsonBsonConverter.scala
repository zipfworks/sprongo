package com.zipfworks.sprongo

import reactivemongo.bson._
import spray.json._
import scala.util.{Failure, Success}
import SprongoConverters._

object JsonBsonConverter {

  def bsonValueToJsValue(bval: BSONValue): JsValue = {
    bval match {
      case v: BSONString    => JsString(v.value)
      case v: BSONLong      => JsNumber(v.value)
      case v: BSONDouble    => JsNumber(v.value)
      case v: BSONInteger   => JsNumber(v.value)
      case v: BSONBoolean   => JsBoolean(v.value)
      case v: BSONArray     => JsArray(v.collect(b => bsonValueToJsValue(b)): _*)
      case v: BSONDocument  => bdocToJsObject(v)
      case v: BSONNull.type => JsNull
      case v: BSONUndefined.type => JsNull
      case v =>
        println(bval.getClass)
        JsString(bval.getClass.toString)
    }
  }

  def bdocToJsObject(doc: BSONDocument): JsObject = {
    val stream = doc.elements.map(element => element._1 match {
      case "_id" => "id" -> bsonValueToJsValue(element._2)
      case x => x -> bsonValueToJsValue(element._2)
    })
    val mapped = stream.toMap
    JsObject(mapped)
  }

  def jsValueToBsonVal(jsVal: JsValue): BSONValue = {
    jsVal match {
      case v: JsString => BSONString(v.value)
      case v: JsNumber => v.value match {
        case num if num.isValidLong   => BSONLong(num.toLong)
        case num if num.isValidDouble => BSONDouble(num.toDouble)
        case num if num.isValidInt    => BSONInteger(num.toInt)
        case num if num.isValidFloat  => BSONDouble(num.toFloat)
        case num if num.isValidShort  => BSONInteger(num.toShortExact)
        case num => BSONDouble(num.toDouble)}
      case v: JsBoolean   => BSONBoolean(v.value)
      case v: JsArray     => BSONArray(v.elements.map(jsValueToBsonVal))
      case v: JsObject    => jsObjToBdoc(v)
      case v: JsNull.type => BSONNull
    }
  }

  def jsObjToBdoc(jsobj: JsObject): BSONDocument = {
    jsobj.fields.foldLeft(BSONDocument())((doc, entry) => {
      val key = entry._1 match {
        case "id" => "_id"
        case x => x
      }
      doc.add(key -> jsValueToBsonVal(entry._2))
    })
  }

}
