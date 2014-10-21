package com.zipfworks.sprongo.macros

import akka.actor.ActorSystem
import com.zipfworks.sprongo.ExtendedJsonProtocol
import org.joda.time.DateTime
import reactivemongo.bson._

object Test {

  val dbURL = Seq("api1-dev.zipfcommerce.com")
  implicit val sys = ActorSystem("test")
  import sys.dispatcher

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

  def main (args: Array[String]) {
    val doc = TestEntry(
      string = "blah",
      int = 0,
      long = 1000000000,
      dt = DateTime.now
    )
    TestDB.TestEntries.find(BSONDocument("_id" -> BSONObjectID("54469594c41c00c41c134082"))).cursor[TestEntry].collect[List](1).map(list => {
      println(list)
    })
  }

}
