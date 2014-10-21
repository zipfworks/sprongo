package com.zipfworks.sprongo.macros

import akka.actor.ActorSystem
import reactivemongo.api.{MongoConnection, MongoDriver}
import reactivemongo.core.nodeset.Authenticate

class MacroDB(nodes: Seq[String], db: String, authentications: Seq[Authenticate] = Seq.empty, nbChannelsPerNode: Int = 10)
             (implicit system: ActorSystem) {

  import system.dispatcher

  val mongoDriver: MongoDriver         = MongoDriver(system)
  val mongoConnection: MongoConnection = mongoDriver
    .connection(nodes = nodes, authentications = authentications, nbChannelsPerNode = nbChannelsPerNode, name = None)

  implicit val defaultDB = mongoConnection(db)

}


