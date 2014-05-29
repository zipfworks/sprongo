package com.zipfworks.sprongo

import reactivemongo.api.MongoDriver
import akka.actor.ActorSystem
import reactivemongo.core.nodeset.Authenticate
import spray.json.RootJsonFormat

class SprongoDB(dbUrl: Seq[String], dbName: String, system: Option[ActorSystem] = None, authenticators: List[Authenticate] = List()) {

  implicit lazy val driver = new MongoDriver(system)
  implicit lazy val exec = driver.system.dispatcher
  implicit lazy val connection = driver.connection(dbUrl, nbChannelsPerNode = 10, authentications = authenticators)
  implicit lazy val db = connection(dbName)

  def getCollection[T <: Model](collName: String)(implicit jsFormat: RootJsonFormat[T]): CollectionDAO[T] = {
    new CollectionDAO[T](collName)
  }

}
