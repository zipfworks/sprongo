package com.zipfworks.sprongo

import com.zipfworks.sprongo.commands.Distinct
import play.api.libs.iteratee.Enumerator
import reactivemongo.api.{Cursor, FailoverStrategy, DB}
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson._
import reactivemongo.core.commands.{LastError, Count}
import scala.concurrent.{ExecutionContext, Future}
import SprongoDSL._

class SprongoCollection(database: DB, collName: String, failover: FailoverStrategy = FailoverStrategy())
  extends BSONCollection(database, collName, failover) {

  implicit private val ec: ExecutionContext = database.connection.actorSystem.dispatcher
  def collection: BSONCollection = this

  /**********************************************************************************
    * Count Command
    *********************************************************************************/
  def exec[T](query: CountQuery[T])(implicit writer: BSONDocumentWriter[T]): Future[Int] = {
    db.command(new Count(collName, Some(writer.write(query.selector))))
  }

  /**********************************************************************************
    * Distinct Command - Eventually move to Dsl so we can pass in readPref
    *********************************************************************************/
  def distinct[T](field: String, query: T = BSONDocument())(implicit writer: BSONDocumentWriter[T]): Future[BSONArray] = {
    db.command(new Distinct(collName, field, Some(writer.write(query))))
  }

  def distinct(field: String, query: Producer[(String, BSONValue)]*): Future[BSONArray] = {
    distinct(field, BSONDocument(query: _*))
  }

  /**********************************************************************************
    * Creates
    *********************************************************************************/
  def exec(c: CreateQuery): Future[LastError] = {
    collection.insert(c.document)
  }

//  def exec[M](c: CreateBulkQuery[M])(implicit writer: BSONDocumentWriter[M]): Future[Int] = {
//    collection.bulkInsert(Enumerator.enumerate(c.ds), bulkSize = c.bulkSize, bulkByteSize = c.bulkByteSize)
//  }

  /**********************************************************************************
    * Updates
    *********************************************************************************/
  def exec(u: UpdateQuery): Future[LastError] = {
    collection.update(
      selector = u.selector,
      update = u.update,
      upsert = u.upsert,
      multi = u.multi)
  }

//  def exec[M](u: UpdateModelQuery[M])(implicit writer: BSONDocumentWriter[M]): Future[LastError]

  /**********************************************************************************
    * Deletes
    *********************************************************************************/
  def exec(d: DeleteQuery): Future[LastError] = {
    collection.remove(
      query = d.s,
      firstMatchOnly = d.firstMatchOnly
    )
  }

//  def exec(d: DeleteModelQuery[M]): Future[LastError]


  /**********************************************************************************
    * Reads
    *********************************************************************************/
  private def getCursor[M](r: ReadQuery)(implicit reader: BSONDocumentReader[M]): Cursor[M] = {
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

  def exec[M](r: ReadQuery)(implicit reader: BSONDocumentReader[M]): Enumerator[M] = {
    val cursor = getCursor[M](r)
    r.limit match {
      case None    => cursor.enumerate()
      case Some(l) => cursor.enumerate(maxDocs = l)
    }
  }

  def exec[M](r: ReadBulkQuery)(implicit reader: BSONDocumentReader[M]): Enumerator[Iterator[M]] = {
    r.q.limit match {
      case None    => getCursor[M](r.q).enumerateBulks()
      case Some(l) => getCursor[M](r.q).enumerateBulks(maxDocs = l)
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

  def exec[M](r: ReadListQuery)(implicit reader: BSONDocumentReader[M]): Future[List[M]] = {
    val cursor = getCursor[M](r.q)
    r.q.limit match {
      case None    => cursor.collect[List]()
      case Some(l) => cursor.collect[List](upTo = l)
    }
  }

  def exec[M](r: ReadOneQuery)(implicit reader: BSONDocumentReader[M]): Future[Option[M]] = {
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
