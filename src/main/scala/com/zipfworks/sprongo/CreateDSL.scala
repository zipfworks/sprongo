package com.zipfworks.sprongo

import reactivemongo.api.bulk
import reactivemongo.bson.BSONDocument
import reactivemongo.core.commands.GetLastError
import spray.json._


case class CreateQuery[T](
  doc: T,
  writeConcern: GetLastError = GetLastError()
){
  def writeConcern(gle: GetLastError): CreateQuery[T] = this.copy(writeConcern = gle)
}

case class CreateBulkQuery[T](
  ds: Seq[T], 
  bulkSize: Int = bulk.MaxDocs, 
  bulkByteSize: Int = bulk.MaxBulkSize,
  writeConcern: GetLastError = GetLastError()
){
  def bulkSize(i: Int): CreateBulkQuery[T] = this.copy(bulkSize = i)
  def bulkByteSize(i: Int): CreateBulkQuery[T] = this.copy(bulkByteSize = i)
  def writeConcern(gle: GetLastError): CreateBulkQuery[T] = this.copy(writeConcern = gle)
}

trait CreateDSL {

  class CreateExpectsDoc {
    def doc[T](d: T): CreateQuery[T] = CreateQuery(d)
    def docs[T](ds: Seq[T]): CreateBulkQuery[T] = CreateBulkQuery(ds)
  }

  def create: CreateExpectsDoc = new CreateExpectsDoc()
}

object CreateDSL extends CreateDSL
