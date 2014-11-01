package com.zipfworks.sprongo

import akka.actor.ActorSystem
import com.zipfworks.sprongo.macros.{ExtendedMacroHandlers, MacroDAO, MacroDB}
import org.joda.time.DateTime
import reactivemongo.bson.{Macros, BSONObjectID}

object Common extends ExtendedMacroHandlers {
  import scala.concurrent.duration._

  val timeout = 10.seconds

  case class TestEntry(
    string: String,
    integer: Int,
    long: Long,
    datetime: DateTime,
    _id: BSONObjectID = BSONObjectID.generate
  )

  object TestEntry {
    implicit val handler = Macros.handler[TestEntry]
  }

  implicit lazy val system = ActorSystem()
  object TestDB extends MacroDB(Seq("localhost"), "test") {
    object InsertTestColl extends MacroDAO[TestEntry]("insert-test")
    object FindTestColl   extends MacroDAO[TestEntry]("find-test")
    object RemoveTestColl extends MacroDAO[TestEntry]("remove-test")
    object UpdateTestColl extends MacroDAO[TestEntry]("update-test")
  }
}
