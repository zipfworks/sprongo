package com.zipfworks.sprongo

import reactivemongo.api.{QueryOpts, ReadPreference}
import reactivemongo.bson._

trait ReadDSL {

  def read: ReadExpectsSelector                = new ReadExpectsSelector()
  def read(s: BSONDocument)                    = new ReadExpectsSelector().selector(s)
  def read(s: Producer[(String, BSONValue)] *) = new ReadExpectsSelector().selector(s: _*)

  class ReadExpectsSelector {

    def selector(s: BSONDocument): ReadQuery = ReadQuery(s)
    def selector(s: Producer[(String, BSONValue)] *): ReadQuery = selector(BSONDocument(s: _*))

    def ids(idList: String*)(implicit a: DummyImplicit): ReadQuery =
      ReadQuery(BSONDocument("_id" -> BSONDocument("$in" -> BSONArray(idList.map(BSONString.apply)))))

    def ids(idList: BSONValue*): ReadQuery =
      ReadQuery(BSONDocument("_id" -> BSONDocument("$in" -> BSONArray(idList))))

    def one(s: BSONDocument): ReadOneQuery =  ReadQuery(s).one
    def id(id: String): ReadOneQuery = ReadQuery(BSONDocument("_id" -> id)).one
    def id(id: BSONValue): ReadOneQuery = ReadQuery(BSONDocument("_id" -> id)).one

  }

  //reads
  case class ReadQuery(
    sel: BSONDocument,
    sort: BSONDocument = BSONDocument(),
    rp: ReadPreference = ReadPreference.secondaryPrefered,
    opts: QueryOpts = QueryOpts().slaveOk,
    limit: Option[Int] = None
  ){
    def projection(fields: Producer[(String, BSONValue)]*): ReadProjectionQuery =
      ReadProjectionQuery(this, BSONDocument(fields: _*))

    def asBulk = ReadBulkQuery(this)
    def asList = ReadListQuery(this)
    def one    = ReadOneQuery(this)

    def sort(s: Producer[(String, BSONValue)]*): ReadQuery = this.copy(sort = BSONDocument(s: _*))
    def sort(s: BSONDocument): ReadQuery = this.copy(sort = s)
    def readPreference(readp: ReadPreference): ReadQuery = this.copy(rp = readp)
    def skip(i: Int): ReadQuery = this.copy(opts = opts.copy(skipN = i))
    def batchSize(i: Int): ReadQuery = this.copy(opts = opts.copy(batchSizeN = i))
    def limit(i: Int): ReadQuery = this.copy(limit = Some(i))
  }

  case class ReadBulkQuery(q: ReadQuery)

  case class ReadListQuery(q: ReadQuery)

  //projections
  case class ReadProjectionQuery(q: ReadQuery, p: BSONDocument){
    def asList = ReadProjectionListQuery(this)
    def asBulk = ReadProjectionBulkQuery(this)
  }

  case class ReadProjectionBulkQuery(r: ReadProjectionQuery)

  case class ReadProjectionListQuery(r: ReadProjectionQuery)

  //read one
  case class ReadOneQuery(q: ReadQuery){
    def readPreference(readp: ReadPreference): ReadOneQuery = this.copy(q = q.readPreference(readp))
    def projection(fields: Producer[(String, BSONValue)]*): ReadOneProjectionQuery = ReadOneProjectionQuery(q, BSONDocument(fields: _*))
    def include(fields: String*): ReadOneProjectionQuery = ReadOneProjectionQuery(q, BSONDocument(fields.map(s => s -> BSONInteger(1))))
  }

  case class ReadOneProjectionQuery(q: ReadQuery, projection: BSONDocument){
    def readPreference(readp: ReadPreference) = this.copy(q = q.readPreference(readp))
  }

}
