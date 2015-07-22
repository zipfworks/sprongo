package com.zipfworks.sprongo.driver

import com.zipfworks.sprongo.{CollectionDAO, Model}
import reactivemongo.api.{MongoConnection, DefaultDB}
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContext

trait DBBuilder {

  val dbName: String
  val connection: MongoConnection

  private def executionContext: ExecutionContext = connection.actorSystem.dispatcher
  private def defaultDB: DefaultDB = connection(dbName)(executionContext)

  def getCollection[M <: Model](collName: String)(implicit js: RootJsonFormat[M]) = {
    new CollectionDAO[M](collName)(ec = executionContext, jsFormat = js, db = defaultDB)
  }

}


