package com.zipfworks.sprongo.macros

import reactivemongo.api.{ReadPreference, QueryOpts}
import reactivemongo.bson.{Producer, BSONValue, BSONDocument}

trait FindDSL {

  /**********************************************************************************
   *  Find One Queries
   *********************************************************************************/
  case class FindOneQuery[S](
    selector: S,
    readPreference: ReadPreference,
    queryOpts: QueryOpts
  ){
    def project[T2](projection: BSONDocument): FindOneQueryProjection[S, T2] = {
      FindOneQueryProjection(
        selector = selector,
        readPreference = readPreference,
        queryOpts = queryOpts,
        projection = projection
      )
    }

    def project[T2](projection: Producer[(String, BSONValue)]*): FindOneQueryProjection[S, T2] = {
      project(BSONDocument(projection: _*))
    }
  }

  case class FindOneQueryProjection[S, T2](
    selector: S,
    readPreference: ReadPreference,
    projection: BSONDocument,
    queryOpts: QueryOpts
  )

  /**********************************************************************************
    *  Find as List Queries
    *********************************************************************************/
  case class FindListQuery[S](
    selector: S,
    readPreference: ReadPreference,
    queryOpts: QueryOpts,
    limit: Int,
    stopOnError: Boolean
  ){
    def project[T2](projection: BSONDocument): FindListQueryProjection[S, T2] = {
      FindListQueryProjection(
        selector = selector,
        readPreference = readPreference,
        queryOpts = queryOpts,
        limit = limit,
        stopOnError = stopOnError,
        projection = projection
      )
    }

    def project[T2](projection: Producer[(String, BSONValue)]*): FindListQueryProjection[S, T2] = {
      project(BSONDocument(projection: _*))
    }
  }

  case class FindListQueryProjection[S, T2](
    selector: S,
    readPreference: ReadPreference,
    queryOpts: QueryOpts,
    limit: Int,
    stopOnError: Boolean,
    projection: BSONDocument
  )

  /**********************************************************************************
    *  Find as Bulk Queries
    *********************************************************************************/
  case class FindBulkQuery[S](
    selector: S,
    readPreference: ReadPreference,
    queryOpts: QueryOpts,
    limit: Int,
    stopOnError: Boolean
  ){
    def batchSize(i: Int): FindBulkQuery[S] = this.copy(queryOpts = queryOpts.batchSize(i))

    def project[T2](projection: BSONDocument): FindBulkQueryProjection[S, T2] = {
      FindBulkQueryProjection(
        selector = selector,
        readPreference = readPreference,
        queryOpts = queryOpts,
        limit = limit,
        stopOnError = stopOnError,
        projection = projection
      )
    }

    def project[T2](projection: Producer[(String, BSONValue)]*): FindBulkQueryProjection[S, T2] = {
      project(BSONDocument(projection: _*))
    }
  }

  case class FindBulkQueryProjection[S, T2](
    selector: S,
    readPreference: ReadPreference,
    queryOpts: QueryOpts,
    limit: Int,
    stopOnError: Boolean,
    projection: BSONDocument
  )

  /**********************************************************************************
    *  Basic Find Queries
    *********************************************************************************/
  case class FindQuery[S](
    selector: S,
    readPreference: ReadPreference = ReadPreference.secondaryPrefered,
    queryOpts: QueryOpts = QueryOpts().slaveOk,
    limit: Int = Int.MaxValue,
    stopOnError: Boolean = true
  ){
    def readPreference(rp: ReadPreference): FindQuery[S] = this.copy(readPreference = rp)
    def queryOpts(qo: QueryOpts): FindQuery[S] = this.copy(queryOpts = qo)
    def limit(i: Int): FindQuery[S] = this.copy(limit = i)
    def stopOnError(b: Boolean): FindQuery[S] = this.copy(stopOnError = b)

    def skip(i: Int): FindQuery[S] = this.copy(queryOpts = queryOpts.skip(i))
    def batchSize(i: Int): FindQuery[S] = this.copy(queryOpts = queryOpts.batchSize(i))
    def flags(i: Int): FindQuery[S] = this.copy(queryOpts = queryOpts.flags(i))

    def tailable: FindQuery[S] = this.copy(queryOpts = queryOpts.tailable)
    def slaveOk: FindQuery[S] = this.copy(queryOpts = queryOpts.slaveOk)
    def oplogReplay: FindQuery[S] = this.copy(queryOpts = queryOpts.oplogReplay)
    def noCursorTimeout: FindQuery[S] = this.copy(queryOpts = queryOpts.noCursorTimeout)

    def awaitData: FindQuery[S] = this.copy(queryOpts = queryOpts.awaitData)
    def exhaust: FindQuery[S] = this.copy(queryOpts = queryOpts.exhaust)
    def partial: FindQuery[S] = this.copy(queryOpts = queryOpts.partial)

    def one: FindOneQuery[S] = {
      FindOneQuery[S](selector = selector, readPreference = readPreference, queryOpts = queryOpts)
    }

    def asBulk: FindBulkQuery[S] = {
      FindBulkQuery[S](
        selector = selector,
        readPreference = readPreference,
        queryOpts = queryOpts,
        limit = limit,
        stopOnError = stopOnError
      )
    }

    def asList: FindListQuery[S] = {
      FindListQuery[S](
        selector = selector,
        readPreference = readPreference,
        queryOpts = queryOpts,
        limit = limit,
        stopOnError = stopOnError
      )
    }

    def project[T2](projection: BSONDocument): FindQueryProjection[S, T2] = {
      FindQueryProjection[S, T2](
        selector = selector,
        readPreference = readPreference,
        queryOpts = queryOpts,
        limit = limit,
        stopOnError = stopOnError,
        projection = projection
      )
    }

    def project[T2](projection: Producer[(String, BSONValue)]*): FindQueryProjection[S, T2] = {
      project(BSONDocument(projection: _*))
    }
  }


  case class FindQueryProjection[S, T2](
    selector: S,
    readPreference: ReadPreference,
    queryOpts: QueryOpts,
    limit: Int,
    stopOnError: Boolean,
    projection: BSONDocument
  )

  /**********************************************************************************
    * DSL
    *********************************************************************************/
  def find[S](s: S): FindQuery[S] = FindQuery[S](selector = s)

}

object FindDSL extends FindDSL
