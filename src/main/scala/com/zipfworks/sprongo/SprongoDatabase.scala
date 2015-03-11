package com.zipfworks.sprongo

import reactivemongo.api.{FailoverStrategy, DefaultDB, MongoConnection}
import reactivemongo.bson.{BSONDocumentWriter, BSONDocumentReader}

trait SprongoDatabase {

  def dbName: String
  def conn: MongoConnection

  def failover: FailoverStrategy = FailoverStrategy()
  def db: DefaultDB = conn(dbName)(conn.actorSystem.dispatcher)

  def getCollection[M](collName: String)
                      (implicit reader: BSONDocumentReader[M], writer: BSONDocumentWriter[M]): SprongoCollection[M] =
    new SprongoCollection[M](db, collName)
}
