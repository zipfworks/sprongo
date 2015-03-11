package com.zipfworks.sprongo

import com.zipfworks.sprongo.commands.Distinct
import play.api.libs.iteratee.Enumerator
import reactivemongo.api.{Cursor, FailoverStrategy, DB}
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson._
import reactivemongo.core.commands.{LastError, Count}
import scala.concurrent.{ExecutionContext, Future}
import SprongoDSL._

class SprongoCollection[M](database: DB, collName: String, failover: FailoverStrategy = FailoverStrategy())
                          (implicit reader: BSONDocumentReader[M], writer: BSONDocumentWriter[M])
  extends BSONCollection(database, collName, failover) {

  implicit private val ec: ExecutionContext = database.connection.actorSystem.dispatcher
  //TODO: get rid of this reference
  def collection: BSONCollection = this

  /**********************************************************************************
    * Count Command
    *********************************************************************************/

  def exec[T](query: CountQuery[T])(implicit writer: BSONDocumentWriter[T]): Future[Int] = {
    database.command(new Count(collName, Some(writer.write(query.selector))))
  }

  /**********************************************************************************
    * Distinct Command
    *********************************************************************************/

  def exec[T](query: DistinctQuery[T])(implicit writer: BSONDocumentWriter[T]): Future[BSONArray] = {
    database.command(new Distinct(collName, query.field, Some(writer.write(query.selector))))
  }

  /**********************************************************************************
    * Create Document
    *********************************************************************************/

  def exec[T](c: CreateQuery[T])(implicit writer: BSONDocumentWriter[T]): Future[LastError] = {
    insert(c.doc, c.writeConcern)
  }

  def exec[T](c: CreateBulkQuery[T])(implicit writer: BSONDocumentWriter[T]): Future[Int] = {
    bulkInsert(
      enumerator = Enumerator.enumerate(c.ds),
      bulkSize = c.bulkSize,
      bulkByteSize = c.bulkByteSize,
      writeConcern = c.writeConcern
    )
  }

  /**********************************************************************************
    * Update Document
    *********************************************************************************/
  def exec(query: UpdateQuery): Future[LastError] = {
    collection.update(
      selector = query.selector,
      update = query.update,
      upsert = query.upsert,
      multi = query.multi,
      writeConcern = query.writeConcern
    )
  }

  /**********************************************************************************
    * Delete Document
    *********************************************************************************/
  def exec(query: DeleteQuery): Future[LastError] = {
    collection.remove(
      query = query.s,
      firstMatchOnly = query.firstMatchOnly,
      writeConcern = query.writeConcern
    )
  }

  /**********************************************************************************
    * Reads
    *********************************************************************************/
  private def getCursor(r: ReadQuery): Cursor[M] = {
    collection
      .find(r.sel)
      .sort(r.sort)
      .options(r.opts)
      .cursor[M](r.rp)
  }

  private def getCursor(r: ReadProjectionQuery): Cursor[BSONDocument] = {
    collection
      .find(r.q.sel, r.p)
      .sort(r.q.sort)
      .options(r.q.opts)
      .cursor[BSONDocument](r.q.rp)
  }

  def exec(r: ReadQuery): Enumerator[M] = {
    val cursor = getCursor(r)
    r.limit match {
      case None    => cursor.enumerate()
      case Some(l) => cursor.enumerate(maxDocs = l)
    }
  }

  def exec(r: ReadBulkQuery): Enumerator[Iterator[M]] = {
    r.q.limit match {
      case None    => getCursor(r.q).enumerateBulks()
      case Some(l) => getCursor(r.q).enumerateBulks(maxDocs = l)
    }
  }

  def exec(r: ReadProjectionQuery): Enumerator[BSONDocument] = {
    r.q.limit match {
      case None    => getCursor(r).enumerate()
      case Some(l) => getCursor(r).enumerate(maxDocs = l)
    }
  }

  def exec(r: ReadProjectionListQuery): Future[List[BSONDocument]] = {
    r.r.q.limit match {
      case None    => getCursor(r.r).collect[List]()
      case Some(l) => getCursor(r.r).collect[List](upTo = l)
    }
  }

  def exec(r: ReadProjectionBulkQuery): Enumerator[Iterator[BSONDocument]] = {
    r.r.q.limit match {
      case None    => getCursor(r.r).enumerateBulks()
      case Some(l) => getCursor(r.r).enumerateBulks(maxDocs = l)
    }
  }

  def exec(r: ReadListQuery): Future[List[M]] = {
    val cursor = getCursor(r.q)
    r.q.limit match {
      case None    => cursor.collect[List]()
      case Some(l) => cursor.collect[List](upTo = l)
    }
  }

  def exec(r: ReadOneQuery): Future[Option[M]] = {
    collection
      .find(r.q.sel)
      .sort(r.q.sort)
      .options(r.q.opts)
      .one[M](readPreference = r.q.rp)
  }

  def exec(r: ReadOneProjectionQuery): Future[Option[BSONDocument]] = {
    collection
      .find(r.q.sel, r.projection)
      .sort(r.q.sort)
      .options(r.q.opts)
      .one[BSONDocument](readPreference = r.q.rp)
  }
}
