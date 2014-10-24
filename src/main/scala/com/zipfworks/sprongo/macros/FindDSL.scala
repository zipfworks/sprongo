package com.zipfworks.sprongo.macros

import reactivemongo.api.QueryOpts
import reactivemongo.bson.{Producer, BSONValue, BSONDocument}

trait FindDSL {

  /**********************************************************************************
   *  Find One Queries
   *********************************************************************************/
  case class FindOneQuery[S](
    selector: S,
    queryOpts: QueryOpts
  ){
    def project[T2](projection: BSONDocument): FindOneQueryProjection[S, T2] = FindOneQueryProjection(
      selector = selector,
      queryOpts = queryOpts,
      projection = projection
    )

    def project[T2](projection: Producer[(String, BSONValue)]*): FindOneQueryProjection[S, T2] = FindOneQueryProjection(
      selector = selector,
      queryOpts = queryOpts,
      projection = BSONDocument(projection: _*)
    )
  }

  case class FindOneQueryProjection[S, T2](
    selector: S,
    projection: BSONDocument,
    queryOpts: QueryOpts
  )

  /**********************************************************************************
    *  Find as List Queries
    *********************************************************************************/
  case class FindListQuery[S](
    selector: S,
    queryOpts: QueryOpts,
    limit: Int,
    stopOnError: Boolean
  )

  /**********************************************************************************
    *  Find as Bulk Queries
    *********************************************************************************/
  case class FindBulkQuery[S](
    selector: S,
    queryOpts: QueryOpts,
    limit: Int,
    stopOnError: Boolean
  ){
    def batchSize(i: Int): FindBulkQuery[S] = this.copy(queryOpts = queryOpts.batchSize(i))
  }

  /**********************************************************************************
    *  Basic Find Queries
    *********************************************************************************/
  case class FindQuery[S](
    selector: S,
    queryOpts: QueryOpts = QueryOpts().slaveOk,
    limit: Int = Int.MaxValue,
    stopOnError: Boolean = true
  ){
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

    def one: FindOneQuery[S] = FindOneQuery[S](selector = selector, queryOpts = queryOpts)

    def asBulk: FindBulkQuery[S] = FindBulkQuery[S](
      selector = selector,
      queryOpts = queryOpts,
      limit = limit,
      stopOnError = stopOnError
    )

    def asList: FindListQuery[S] = FindListQuery[S](
      selector = selector,
      queryOpts = queryOpts,
      limit = limit,
      stopOnError = stopOnError
    )


  }

}

object FindDSL extends FindDSL
