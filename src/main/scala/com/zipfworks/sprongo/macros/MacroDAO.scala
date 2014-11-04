package com.zipfworks.sprongo.macros

import com.zipfworks.sprongo.commands.Distinct
import SprongoDSL._
import play.api.libs.iteratee.Enumerator
import reactivemongo.api.{DefaultDB, FailoverStrategy}
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson._
import reactivemongo.core.commands.{LastError, Count}
import scala.concurrent.{ExecutionContext, Future}

class MacroDAO[T](coll_name: String)(implicit db: DefaultDB, writer: BSONDocumentWriter[T],
                                     reader: BSONDocumentReader[T], ec: ExecutionContext)
  extends BSONCollection(db, coll_name, FailoverStrategy()) {

  /** Count Command **/
  def execute(countCMD: CountCommand): Future[Int] =
    db.command(new Count(coll_name, Some(countCMD.s)))

  /** Distinct Command **/
  def execute(distinctCMD: DistinctCommand): Future[BSONArray] =
    db.command(new Distinct(coll_name, distinctCMD.field, distinctCMD.selector))

  /** Remove Documents **/
  def execute[S](removeCMD: RemoveQuery): Future[LastError] = {
    remove(removeCMD.selector, removeCMD.writeConcern, !removeCMD.multi)
  }

  /** Update Documents **/
  def execute(updateCMD: UpdateQuery): Future[LastError] = {
    update(updateCMD.selector, updateCMD.update, updateCMD.writeConcern, updateCMD.upsert, updateCMD.multi)
  }

  /** Insert Single Document **/
  def execute(insertCMD: InsertDocumentQuery): Future[LastError] = {
    insert(insertCMD.document, insertCMD.writeConcern)
  }

  def execute(insertCMD: InsertModelQuery[T]): Future[LastError] = {
    insert(insertCMD.model, insertCMD.writeConcern)
  }

  /** Bulk Insert Documents **/
  def execute(cmd: InsertModelsQuery[T]): Future[Int] = {
    bulkInsert(enumerator = Enumerator(cmd.models: _*), bulkSize = cmd.bulkSize, bulkByteSize = cmd.bulkByteSize)
  }

  /** Find Documents **/
  //return enumerator
  def execute[S](cmd: FindQuery[S])(implicit selWriter: BSONDocumentWriter[S]): Enumerator[T] = {
    find(cmd.selector).options(cmd.queryOpts).cursor[T](cmd.readPreference).enumerate(cmd.limit, cmd.stopOnError)
  }

  def execute[S, T2](cmd: FindQueryProjection[S, T2])
                    (implicit selWriter: BSONDocumentWriter[S], proReader: BSONDocumentReader[T2]) = {
    find(cmd.selector).options(cmd.queryOpts).cursor[T2](cmd.readPreference).enumerate(cmd.limit, cmd.stopOnError)
  }

  //return enumerator bulk
  def execute[S](cmd: FindBulkQuery[S])
                (implicit selWriter: BSONDocumentWriter[S]): Enumerator[Iterator[T]] = {
    find(cmd.selector).options(cmd.queryOpts).cursor[T](cmd.readPreference).enumerateBulks(cmd.limit, cmd.stopOnError)
  }

  def execute[S, T2](cmd: FindBulkQueryProjection[S, T2])
                    (implicit selWriter: BSONDocumentWriter[S], proReader: BSONDocumentReader[T2]) = {
    find(cmd.selector).options(cmd.queryOpts).cursor[T2](cmd.readPreference).enumerateBulks(cmd.limit, cmd.stopOnError)
  }

  //return List[T]
  def execute[S](cmd: FindListQuery[S])
                (implicit selWriter: BSONDocumentWriter[S]): Future[List[T]] = {
    find(cmd.selector).options(cmd.queryOpts).cursor[T](cmd.readPreference).collect[List](cmd.limit, cmd.stopOnError)
  }

  def execute[S, T2](cmd: FindListQueryProjection[S, T2])
                    (implicit selWriter: BSONDocumentWriter[S], proReader: BSONDocumentReader[T2]) = {
    find(cmd.selector).options(cmd.queryOpts).cursor[T2](cmd.readPreference).collect[List](cmd.limit, cmd.stopOnError)
  }

  //return one Option[T]
  def execute[S](cmd: FindOneQuery[S])
                (implicit selWriter: BSONDocumentWriter[S]): Future[Option[T]] = {
    find(cmd.selector).options(cmd.queryOpts).one[T](cmd.readPreference)
  }

  def execute[S, T2](cmd: FindOneQueryProjection[S, T2])
                    (implicit selWriter: BSONDocumentWriter[S], proReader: BSONDocumentReader[T2]) = {
    find(cmd.selector).options(cmd.queryOpts).one[T2](cmd.readPreference)
  }

}


