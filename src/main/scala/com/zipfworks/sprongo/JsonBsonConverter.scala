package com.zipfworks.sprongo

import reactivemongo.bson._
import spray.json._
import scala.util.{Failure, Success}

object JsonBsonConverter {

  def bsonValueToJsValue(bval: BSONValue): JsValue = {
    bval match {
      case _: BSONString => JsString(bval.asInstanceOf[BSONString].value)
      case _: BSONLong => JsNumber(bval.asInstanceOf[BSONLong].value)
      case _: BSONDouble => JsNumber(bval.asInstanceOf[BSONDouble].value)
      case _: BSONInteger => JsNumber(bval.asInstanceOf[BSONInteger].value)
      case _: BSONBoolean => JsBoolean(bval.asInstanceOf[BSONBoolean].value)
      case _: BSONArray => JsArray(
        bval.asInstanceOf[BSONArray].stream.flatMap({
          case Success(v) => Some(v)
          case Failure(v) => None
        }).map(v => bsonValueToJsValue(v)).toVector)
      case _: BSONDocument => bdocToJsObject(bval.asInstanceOf[BSONDocument])
      case _: BSONNull.type => JsNull
      case _: BSONUndefined.type => JsNull
      case _ =>
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
      case _: JsString => BSONString(jsVal.asInstanceOf[JsString].value)
      case _: JsNumber => jsVal.asInstanceOf[JsNumber].value match {
        case num if num.isValidLong => BSONLong(num.toLong)
        case num if num.isValidDouble => BSONDouble(num.toDouble)
        case num if num.isValidInt => BSONInteger(num.toInt)
        case num if num.isValidFloat => BSONDouble(num.toFloat)
        case num if num.isValidShort => BSONInteger(num.toShortExact)
        case num => BSONDouble(num.toDouble)}
      case _: JsBoolean => BSONBoolean(jsVal.asInstanceOf[JsBoolean].value)
      case _: JsArray => jsVal.asInstanceOf[JsArray].elements.foldLeft(BSONArray())((arr, jsval) => arr.add(jsValueToBsonVal(jsval)))
      case _: JsObject => jsObjToBdoc(jsVal.asJsObject)
      case _: JsNull.type => BSONNull
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
