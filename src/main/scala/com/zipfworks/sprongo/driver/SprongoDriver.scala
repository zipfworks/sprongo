package com.zipfworks.sprongo.driver

import akka.actor.ActorSystem
import reactivemongo.api.MongoDriver
import reactivemongo.core.nodeset.Authenticate

case class SprongoDriver(
  nodes: Seq[String],
  auth: List[Authenticate] = Nil,
  numChannelsPerNode: Int = 10
)(implicit system: ActorSystem) {
  val driver = new MongoDriver(system)
  val connection = driver.connection(nodes, nbChannelsPerNode = numChannelsPerNode, authentications = auth)
}
