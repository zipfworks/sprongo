package com.zipfworks.sprongo.macros

import reactivemongo.bson.{BSONDocumentWriter, BSONDocument}
import reactivemongo.core.commands.GetLastError

trait InsertDSL {

  sealed trait InsertQuery { def writeConcern: GetLastError }

  case class InsertDocumentQuery(
    document: BSONDocument,
    writeConcern: GetLastError = GetLastError()
  ) extends InsertQuery {
    def writeConcern(wc: GetLastError): InsertDocumentQuery = this.copy(writeConcern = wc)
  }

  case class InsertModelQuery[T](
    model: T,
    writeConcern: GetLastError = GetLastError()
  )(implicit writer: BSONDocumentWriter[T]) extends InsertQuery {
    def writeConcern(wc: GetLastError): InsertModelQuery[T] = this.copy(writeConcern = wc)
  }

  case class InsertModelsQuery[T](
    models: Seq[T],
    bulkSize: Int = reactivemongo.api.bulk.MaxDocs,
    bulkByteSize: Int = reactivemongo.api.bulk.MaxBulkSize
  )(implicit writer: BSONDocumentWriter[T]) {
    def bulkSize(s: Int): InsertModelsQuery[T] = this.copy(bulkSize = s)
    def bulkByteSize(s: Int): InsertModelsQuery[T] = this.copy(bulkByteSize = s)
  }

  def insert(document: BSONDocument) =
    InsertDocumentQuery(document = document)

  def insert[T](model: T)(implicit writer: BSONDocumentWriter[T]) =
    InsertModelQuery(model)

  def insert[T](models: Seq[T])(implicit writer: BSONDocumentWriter[T]) =
    InsertModelsQuery(models)

}

object InsertDSL extends InsertDSL
