package com.zipfworks.sprongo.macros

import akka.actor.ActorSystem
import reactivemongo.api.{MongoConnection, MongoDriver}

class MacroDB(nodes: Seq[String], db: String)(implicit system: ActorSystem) {

  import system.dispatcher

  val mongoDriver: MongoDriver         = MongoDriver(system)
  val mongoConnection: MongoConnection = mongoDriver.connection(nodes = nodes)

  implicit val defaultDB = mongoConnection(db)
}
