package com.zipfworks.sprongo

import java.util.UUID

import akka.actor.ActorSystem
import reactivemongo.api.MongoConnection
import reactivemongo.bson.Macros
import reactivemongo.bson.Macros.Annotations.Key

object TestRun {
  case class TestModel(@Key("_id") id: String = UUID.randomUUID().toString)
  object TestModel {
    implicit val handler = Macros.handler[TestModel]
  }

  case class Database(conn: MongoConnection) extends SprongoDatabase {
    override def dbName: String = "test"
    val testColl = new SprongoCollection[TestModel](db, "test_models")
  }

  case class Connector(sys: ActorSystem) extends SprongoConnector {
    override def nodes: Seq[String] = Seq("localhost")
    val TestDB = Database(connection)
  }

  def main (args: Array[String]) {
    val sys = ActorSystem()
    import sys.dispatcher
    val conn = Connector(sys)

    conn.TestDB.testColl.exec(SprongoDSL.read().one).map({
      case None => println("none found")
      case Some(d) => println(d)
    })
  }

}
