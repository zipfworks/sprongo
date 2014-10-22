package com.zipfworks.sprongo

import reactivemongo.bson._
import spray.json.{JsArray, JsonReader}
import scala.util.{Failure, Success}

trait SprongoConverters {

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

    def collect[T](implicit f: BSONReader[BSONValue, T]): List[T] = {
      arr.stream.toList.map(_.toOption).flatten.map(f.read)
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

      JsArray(convertedArray)
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

  }

}

object SprongoConverters extends SprongoConverters

