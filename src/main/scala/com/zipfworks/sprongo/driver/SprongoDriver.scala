package com.zipfworks.sprongo.driver

import com.typesafe.config.Config
import reactivemongo.api.{MongoConnectionOptions, MongoDriver}
import reactivemongo.core.nodeset.Authenticate

case class SprongoDriver(
  nodes: Seq[String],
  auth: Seq[Authenticate] = Seq.empty,
  options: MongoConnectionOptions = MongoConnectionOptions(),
  name: Option[String] = None,
  config: Option[Config] = None
){
  //creating drivers and connections aren't free... so they MUST BE A _VAL_
  val driver = new MongoDriver()
  val connection = driver.connection(
    nodes = nodes,
    options = options,
    authentications = auth,
    name = name
  )
}
