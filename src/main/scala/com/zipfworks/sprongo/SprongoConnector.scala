package com.zipfworks.sprongo

import akka.actor.ActorSystem
import reactivemongo.api.{MongoConnection, MongoConnectionOptions, MongoDriver}
import reactivemongo.core.nodeset.Authenticate

trait SprongoConnector{
  def sys: ActorSystem
  def nodes: Seq[String]

  def auth: Seq[Authenticate] = Seq.empty[Authenticate]
  def opts: MongoConnectionOptions = MongoConnectionOptions()
  def channels: Int = 10

  def driver: MongoDriver = new MongoDriver(sys)
  def connection: MongoConnection = driver.connection(nodes, opts, auth, channels)
}
