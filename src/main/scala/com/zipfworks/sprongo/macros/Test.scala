package com.zipfworks.sprongo.macros

import akka.actor.ActorSystem
import com.zipfworks.sprongo.ExtendedJsonProtocol
import org.joda.time.DateTime
import reactivemongo.bson._

object Test {

  val dbURL = Seq("api1-dev.zipfcommerce.com")
  implicit val sys = ActorSystem("test")

  case class TestEntry(
    _id: BSONObjectID = BSONObjectID.generate,
    string: String,
    int: Int,
    long: Long,
    dt: DateTime
  )

  object TestEntry extends ExtendedMacroHandlers with ExtendedJsonProtocol {
    implicit val handler = Macros.handler[TestEntry]
    implicit val jsonFmt = jsonFormat5(TestEntry.apply)
  }


  object TestDB extends MacroDB(dbURL, "test"){
    object TestEntries extends MacroDAO[TestEntry]("foobar")
  }

  def main (args: Array[String]): Unit = {
    import sys.dispatcher
    import InsertDSL._

    val model = Seq(
      TestEntry(string = "test", int = 0, long = 100000, dt = DateTime.now),
      TestEntry(string = "test", int = 0, long = 100000, dt = DateTime.now),
      TestEntry(string = "test", int = 0, long = 100000, dt = DateTime.now),
      TestEntry(string = "test", int = 0, long = 100000, dt = DateTime.now),
      TestEntry(string = "test", int = 0, long = 100000, dt = DateTime.now),
      TestEntry(string = "test", int = 0, long = 100000, dt = DateTime.now)
    )

    TestDB.TestEntries.execute(insert(model)).map(_ => println("done"))
  }

}
