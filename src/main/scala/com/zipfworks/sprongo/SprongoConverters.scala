package com.zipfworks.sprongo

import org.joda.time.DateTime
import reactivemongo.bson._
import spray.json.{JsValue, RootJsonFormat, JsArray, JsonReader}
import scala.util.{Failure, Success}

trait SprongoConverters {

  implicit object IntTupleHandler extends BSONWriter[(Int, Int), BSONArray] with BSONReader[BSONArray, (Int, Int)] {
    override def write(t: (Int, Int)): BSONArray = BSONArray(BSONInteger(t._1), BSONInteger(t._2))
    override def read(bson: BSONArray): (Int, Int) = (bson.getAs[BSONInteger](0).get.value, bson.getAs[BSONInteger](1).get.value)
  }

  implicit object JodaDateTimeHandler extends BSONWriter[DateTime, BSONLong] with BSONReader[BSONLong, DateTime] {
    override def write(t: DateTime): BSONLong = BSONLong(t.getMillis)
    override def read(bson: BSONLong): DateTime = new DateTime(bson.value)
  }

  implicit object JsValueConverter extends RootJsonFormat[BSONValue] {
    override def read(json: JsValue): BSONValue = JsonBsonConverter.jsValueToBsonVal(json)
    override def write(obj: BSONValue): JsValue = JsonBsonConverter.bsonValueToJsValue(obj)
  }

  implicit class BSONArrayPimps(arr: BSONArray){

    /**
     * Collect elements of the array into a List
     * @param f BSONValue to T conversion function
     * @tparam T whatever you want to come out in the list
     * @return List[T]
     */
    def collect[T](f: BSONValue => T): List[T]  = {
      arr.stream.toList.map({
        case Success(v) => Some(f(v))
        case Failure(_) => None
      }).flatten
    }

    /**
     * Convert BSONArray to a spray.json.JsArray
     * @return JsArray
     */
    def asJsArray: JsArray = {
      val convertedArray = arr.stream.toList.map({
        case Success(v) => Some(JsonBsonConverter.bsonValueToJsValue(v))
        case Failure(_) => None
      }).flatten

      JsArray(convertedArray: _*)
    }


  }

  implicit class BSONDocumentPimps(doc: BSONDocument){

    /**
     * Convert this BSONDocument into a T
     * @param writer JsonReader for converting the BSONDocument
     * @tparam T whatever you want out of this
     * @return T whatever you wanted out of this
     */
    def convertTo[T](implicit writer: JsonReader[T]): T =
      JsonBsonConverter.bdocToJsObject(doc).convertTo[T]

    /**
     * Convert this BSONDocument into a spray.json.JsObject
     * @return JsObject representation of this BSONDocument
     */
    def asJsObject =
      JsonBsonConverter.bdocToJsObject(doc)

    /**
     * Convert this BSONDocument into a Map[String, JsValue]
     * @return Map[String, JsValue]
     */
    def asMap =
      asJsObject.fields

  }

}

object SprongoConverters extends SprongoConverters

