package com.zipfworks.sprongo.macros

import reactivemongo.api.{ReadPreference, QueryOpts}
import reactivemongo.bson.{BSONWriter, Producer, BSONValue, BSONDocument}

trait FindDSL {

  /**********************************************************************************
   *  Find One Queries
   *********************************************************************************/
  case class FindOneQuery(
    selector: BSONDocument,
    readPreference: ReadPreference,
    queryOpts: QueryOpts
  ){
    def project[T2](projection: BSONDocument): FindOneQueryProjection[T2] = {
      FindOneQueryProjection(
        selector = selector,
        readPreference = readPreference,
        queryOpts = queryOpts,
        projection = projection
      )
    }

    def project[T2](projection: Producer[(String, BSONValue)]*): FindOneQueryProjection[T2] = {
      project(BSONDocument(projection: _*))
    }
  }

  case class FindOneQueryProjection[T2](
    selector: BSONDocument,
    readPreference: ReadPreference,
    projection: BSONDocument,
    queryOpts: QueryOpts
  )

  /**********************************************************************************
    *  Find as List Queries
    *********************************************************************************/
  case class FindListQuery(
    selector: BSONDocument,
    readPreference: ReadPreference,
    queryOpts: QueryOpts,
    limit: Int,
    stopOnError: Boolean
  ){
    def project[T2](projection: BSONDocument): FindListQueryProjection[T2] = {
      FindListQueryProjection(
        selector = selector,
        readPreference = readPreference,
        queryOpts = queryOpts,
        limit = limit,
        stopOnError = stopOnError,
        projection = projection
      )
    }

    def project[T2](projection: Producer[(String, BSONValue)]*): FindListQueryProjection[T2] = {
      project(BSONDocument(projection: _*))
    }
  }

  case class FindListQueryProjection[T2](
    selector: BSONDocument,
    readPreference: ReadPreference,
    queryOpts: QueryOpts,
    limit: Int,
    stopOnError: Boolean,
    projection: BSONDocument
  )

  /**********************************************************************************
    *  Find as Bulk Queries
    *********************************************************************************/
  case class FindBulkQuery(
    selector: BSONDocument,
    readPreference: ReadPreference,
    queryOpts: QueryOpts,
    limit: Int,
    stopOnError: Boolean
  ){
    def batchSize(i: Int): FindBulkQuery = this.copy(queryOpts = queryOpts.batchSize(i))

    def project[T2](projection: BSONDocument): FindBulkQueryProjection[T2] = {
      FindBulkQueryProjection(
        selector = selector,
        readPreference = readPreference,
        queryOpts = queryOpts,
        limit = limit,
        stopOnError = stopOnError,
        projection = projection
      )
    }

    def project[T2](projection: Producer[(String, BSONValue)]*): FindBulkQueryProjection[T2] = {
      project(BSONDocument(projection: _*))
    }
  }

  case class FindBulkQueryProjection[T2](
    selector: BSONDocument,
    readPreference: ReadPreference,
    queryOpts: QueryOpts,
    limit: Int,
    stopOnError: Boolean,
    projection: BSONDocument
  )

  /**********************************************************************************
    *  Basic Find Queries
    *********************************************************************************/
  case class FindQuery(
    selector: BSONDocument,
    readPreference: ReadPreference = ReadPreference.secondaryPrefered,
    queryOpts: QueryOpts = QueryOpts().slaveOk,
    limit: Int = Int.MaxValue,
    stopOnError: Boolean = true
  ){
    def readPreference(rp: ReadPreference): FindQuery = this.copy(readPreference = rp)
    def queryOpts(qo: QueryOpts): FindQuery = this.copy(queryOpts = qo)
    def limit(i: Int): FindQuery = this.copy(limit = i)
    def stopOnError(b: Boolean): FindQuery = this.copy(stopOnError = b)

    def skip(i: Int): FindQuery = this.copy(queryOpts = queryOpts.skip(i))
    def batchSize(i: Int): FindQuery = this.copy(queryOpts = queryOpts.batchSize(i))
    def flags(i: Int): FindQuery = this.copy(queryOpts = queryOpts.flags(i))

    def tailable: FindQuery = this.copy(queryOpts = queryOpts.tailable)
    def slaveOk: FindQuery = this.copy(queryOpts = queryOpts.slaveOk)
    def oplogReplay: FindQuery = this.copy(queryOpts = queryOpts.oplogReplay)
    def noCursorTimeout: FindQuery = this.copy(queryOpts = queryOpts.noCursorTimeout)

    def awaitData: FindQuery = this.copy(queryOpts = queryOpts.awaitData)
    def exhaust: FindQuery = this.copy(queryOpts = queryOpts.exhaust)
    def partial: FindQuery = this.copy(queryOpts = queryOpts.partial)

    def one: FindOneQuery = {
      FindOneQuery(selector = selector, readPreference = readPreference, queryOpts = queryOpts)
    }

    def asBulk: FindBulkQuery = {
      FindBulkQuery(
        selector = selector,
        readPreference = readPreference,
        queryOpts = queryOpts,
        limit = limit,
        stopOnError = stopOnError
      )
    }

    def asList: FindListQuery = {
      FindListQuery(
        selector = selector,
        readPreference = readPreference,
        queryOpts = queryOpts,
        limit = limit,
        stopOnError = stopOnError
      )
    }

    def project[T2](projection: BSONDocument): FindQueryProjection[T2] = {
      FindQueryProjection[T2](
        selector = selector,
        readPreference = readPreference,
        queryOpts = queryOpts,
        limit = limit,
        stopOnError = stopOnError,
        projection = projection
      )
    }

    def project[T2](projection: Producer[(String, BSONValue)]*): FindQueryProjection[T2] = {
      project(BSONDocument(projection: _*))
    }
  }


  case class FindQueryProjection[T2](
    selector: BSONDocument,
    readPreference: ReadPreference,
    queryOpts: QueryOpts,
    limit: Int,
    stopOnError: Boolean,
    projection: BSONDocument
  )

  /**********************************************************************************
    * DSL
    *********************************************************************************/
  //find("some-id"); find(BSONObjectId("some-id"))
  def find[S](selector: S)(implicit selWriter: SelWriter[S]) =
    FindQuery(selector = selWriter.write(selector))

  //find(MyCaseClassModel("cool!"))
  def find[A <: ModelSelWriter](selector: A) =
    FindQuery(selector = selector.write)

  //find("key" -> "value", "key2" -> "value2")
  def find(selector: Producer[(String, BSONValue)]*) =
    FindQuery(selector = BSONDocument(selector: _*))

  //find("key" -> "value")
  def find[B](selector: (String, B))(implicit writer: BSONWriter[B, _ <: BSONValue]) =
    FindQuery(selector = BSONDocument(Producer.nameValue2Producer(selector)))

}

object FindDSL extends FindDSL
