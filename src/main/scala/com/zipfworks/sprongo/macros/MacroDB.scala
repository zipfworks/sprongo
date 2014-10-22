package com.zipfworks.sprongo.macros

import akka.actor.ActorSystem
import reactivemongo.api.{MongoConnection, MongoDriver}
import reactivemongo.core.nodeset.Authenticate

import scala.concurrent.ExecutionContext

class MacroDB(
               nodes: Seq[String],
               db: String,
               authentications: Seq[Authenticate] = Seq.empty,
               nbChannelsPerNode: Int = 10,
               name: Option[String] = None
             )
             (implicit system: ActorSystem) {

  implicit val ec: ExecutionContext = system.dispatcher

  val mongoDriver: MongoDriver         = MongoDriver(system)
  val mongoConnection: MongoConnection = mongoDriver
    .connection(nodes = nodes, authentications = authentications, nbChannelsPerNode = nbChannelsPerNode, name = name)

  implicit val defaultDB = mongoConnection(db)

}


