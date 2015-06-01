package com.zipfworks.sprongo.driver

import com.zipfworks.sprongo.{CollectionDAO, Model}
import reactivemongo.api.DefaultDB
import spray.json.RootJsonFormat

import scala.concurrent.ExecutionContext

trait DBBuilder {

  def dbName: String
  def driver: SprongoDriver

  private def executionContext: ExecutionContext = driver.driver.system.dispatcher

  def defaultDB: DefaultDB = driver.connection(dbName)(executionContext)

  def getCollection[M <: Model](collName: String)(implicit js: RootJsonFormat[M]) = {
    new CollectionDAO[M](collName)(ec = executionContext, jsFormat = js, db = defaultDB)
  }

}
