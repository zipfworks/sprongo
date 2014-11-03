package com.zipfworks.sprongo

import akka.actor.ActorSystem
import com.zipfworks.sprongo.macros._
import org.joda.time.DateTime
import reactivemongo.bson.{BSONDocument, Macros, BSONObjectID}

object Common extends ExtendedMacroHandlers {
  import scala.concurrent.duration._

  val timeout = 10.seconds

  case class TestEntry(
    string: String,
    integer: Int,
    long: Long,
    datetime: DateTime,
    _id: BSONObjectID = BSONObjectID.generate
  ) extends ObjectIDModel

  object TestEntry {
    implicit val handler = Macros.handler[TestEntry]
//    implicit case object selWriter extends SelWriter[TestEntry] {
//      override def write(t: TestEntry): BSONDocument = BSONDocument("_id" -> t._id)
//    }
  }

  implicit lazy val system = ActorSystem()
  object TestDB extends MacroDB(Seq("localhost"), "test") {
    object InsertTestColl extends MacroDAO[TestEntry]("insert-test")
    object FindTestColl   extends MacroDAO[TestEntry]("find-test")
    object RemoveTestColl extends MacroDAO[TestEntry]("remove-test")
    object UpdateTestColl extends MacroDAO[TestEntry]("update-test")
  }
}
