package com.zipfworks.sprongo.macros

import reactivemongo.api.QueryOpts
import reactivemongo.bson.BSONDocumentWriter

trait FindDSL {

  case class FindQuery[S, P](
    selector: S,
    limit: Option[Int] = None,
    projection: Option[P] = None,
    queryOpts: QueryOpts = QueryOpts().slaveOk
  )(implicit
    selWriter: BSONDocumentWriter[S],
    proWriter: BSONDocumentWriter[P]
  ){
    def queryOpts(qo: QueryOpts): FindQuery[S, P] = this.copy(queryOpts = qo)
    def skip(i: Int): FindQuery[S, P] = this.copy(queryOpts = queryOpts.skip(i))
    def batchSize(i: Int): FindQuery[S, P] = this.copy(queryOpts = queryOpts.batchSize(i))
    def flags(i: Int): FindQuery[S, P] = this.copy(queryOpts = queryOpts.flags(i))

    def tailable: FindQuery[S, P] = this.copy(queryOpts = queryOpts.tailable)
    def slaveOk: FindQuery[S, P] = this.copy(queryOpts = queryOpts.slaveOk)
    def oplogReplay: FindQuery[S, P] = this.copy(queryOpts = queryOpts.oplogReplay)
    def noCursorTimeout: FindQuery[S, P] = this.copy(queryOpts = queryOpts.noCursorTimeout)

    def awaitData: FindQuery[S, P] = this.copy(queryOpts = queryOpts.awaitData)
    def exhaust: FindQuery[S, P] = this.copy(queryOpts = queryOpts.exhaust)
    def partial: FindQuery[S, P] = this.copy(queryOpts = queryOpts.partial)
  }

}

object FindDSL extends FindDSL
