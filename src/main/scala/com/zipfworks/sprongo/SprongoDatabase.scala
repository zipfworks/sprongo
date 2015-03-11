package com.zipfworks.sprongo

import reactivemongo.api.{FailoverStrategy, DefaultDB, MongoConnection}

trait SprongoDatabase {

  def dbName: String
  def conn: MongoConnection

  def failover: FailoverStrategy = FailoverStrategy()
  def db: DefaultDB = conn(dbName)(conn.actorSystem.dispatcher)

}
