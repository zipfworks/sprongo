package com.zipfworks.sprongo.macros

import com.zipfworks.sprongo.commands.Distinct
import com.zipfworks.sprongo.macros.CommandDSL.{DistinctCommand, CountCommand}
import com.zipfworks.sprongo.macros.FindDSL.FindQuery
import com.zipfworks.sprongo.macros.InsertDSL.{InsertModelsQuery, InsertModelQuery, InsertDocumentQuery, InsertQuery}
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

  /** Insert Single Document **/
  def execute(insertCMD: InsertQuery): Future[LastError] = insertCMD match {
    case cmd: InsertDocumentQuery => insert(cmd.document, cmd.writeConcern)
    case cmd: InsertModelQuery[T] => insert(cmd.model, cmd.writeConcern)
  }

  /** Bulk Insert Documents **/
  def execute(cmd: InsertModelsQuery[T]): Future[Int] = {
    bulkInsert(enumerator = Enumerator(cmd.models: _*), bulkSize = cmd.bulkSize, bulkByteSize = cmd.bulkByteSize)
  }

  /** Find Documents **/

}


