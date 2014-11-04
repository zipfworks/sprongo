package com.zipfworks.sprongo

import org.specs2.mutable.Specification
import reactivemongo.bson._
import com.zipfworks.sprongo.macros.SprongoDSL._

import scala.concurrent.Await

class UpsertDSLSpec extends Specification {

  import Common._
  import com.zipfworks.sprongo.macros.SelWriters._

  private def getDoc(id: String): BSONDocument = {
    val find = TestDB.UpdateTestColl.find(BSONDocument("_id" -> id)).one.map(_.get)
    Await.result(find, timeout)
  }

  sequential

  step({
    Await.ready(TestDB.UpdateTestColl.drop(), timeout)
    Await.ready(TestDB.UpdateTestColl.insert(BSONDocument("_id" -> "1", "orig" -> "orig")), timeout)
    Await.ready(TestDB.UpdateTestColl.insert(BSONDocument("_id" -> "2", "orig" -> "orig")), timeout)
    Await.ready(TestDB.UpdateTestColl.insert(BSONDocument("_id" -> "3", "orig" -> "orig")), timeout)
    Await.ready(TestDB.UpdateTestColl.insert(BSONDocument("_id" -> "4")), timeout)
    Await.ready(TestDB.UpdateTestColl.insert(BSONDocument("_id" -> "5", "orig" -> BSONArray())), timeout)
    Await.ready(TestDB.UpdateTestColl.insert(BSONDocument("_id" -> "6")), timeout)
    Await.ready(TestDB.UpdateTestColl.insert(BSONDocument("_id" -> "7", "orig" -> BSONArray("hello"))), timeout)
    Await.ready(TestDB.UpdateTestColl.insert(BSONDocument("_id" -> "8", "orig" -> BSONArray(1, 2, 3, 4))), timeout)

    Await.ready(TestDB.UpdateTestColl.insert(BSONDocument("k" -> "v")), timeout)
    Await.ready(TestDB.UpdateTestColl.insert(BSONDocument("k" -> "v")), timeout)
    Await.ready(TestDB.UpdateTestColl.insert(BSONDocument("k" -> "v")), timeout)
    Await.ready(TestDB.UpdateTestColl.insert(BSONDocument("k" -> "v")), timeout)
  })

  "UpdateDSL" should {
    "completely update a model" in {
      Await.result(TestDB.UpdateTestColl.execute(update("1").ops(BSONDocument("key" -> "value"))), timeout).n mustEqual 1
      val updatedDoc = getDoc("1")
      updatedDoc.get("key") mustEqual Some(BSONString("value"))
      updatedDoc.get("orig") mustEqual None
    }
    "set a piece of the model" in {
      Await.result(TestDB.UpdateTestColl.execute(update("2").ops($set("key", "value"), $set("key2", 111))), timeout).n mustEqual 1
      val updatedDoc = getDoc("2")
      updatedDoc.get("key") mustEqual Some(BSONString("value"))
      updatedDoc.get("key2") mustEqual Some(BSONInteger(111))
      updatedDoc.get("orig") mustEqual Some(BSONString("orig"))
    }
    "unset a piece of the model" in {
      Await.result(TestDB.UpdateTestColl.execute(update("3").ops($unset("orig"))), timeout).n mustEqual 1
      val updatedDoc = getDoc("3")
      updatedDoc.get("orig") mustEqual None
    }
    "increment a field" in {
      Await.result(TestDB.UpdateTestColl.execute(update("4").ops($inc("key", 1))), timeout).n mustEqual 1
      getDoc("4").get("key") mustEqual Some(BSONInteger(1))
      Await.result(TestDB.UpdateTestColl.execute(update("4").ops($inc("key", -1))), timeout).n mustEqual 1
      getDoc("4").get("key") mustEqual Some(BSONInteger(0))
    }
    "push/pull a value onto an array" in {
      Await.result(TestDB.UpdateTestColl.execute(update("5").ops($push("orig", "hello"))), timeout).n mustEqual 1
      getDoc("5").get("orig") mustEqual Some(BSONArray(BSONString("hello")))
      Await.result(TestDB.UpdateTestColl.execute(update("5").ops($pull("orig", "hello"))), timeout).n mustEqual 1
      getDoc("5").get("orig") mustEqual Some(BSONArray())
    }
    "push/pull a document onto an array" in {
      Await.result(TestDB.UpdateTestColl.execute(update("6").ops($push("orig", BSONDocument("key" -> "value")))), timeout).n mustEqual 1
      getDoc("6").get("orig") mustEqual Some(BSONArray(BSONDocument("key" -> "value")))
      Await.result(TestDB.UpdateTestColl.execute(update("6").ops($pull("orig", BSONDocument("key" -> "value")))), timeout).n mustEqual 1
      getDoc("6").get("orig") mustEqual Some(BSONArray())
    }
    "add to a set" in {
      Await.result(TestDB.UpdateTestColl.execute(update("7").ops($addToSet("orig", "hello"))), timeout).n mustEqual 1
      getDoc("7").get("orig") mustEqual Some(BSONArray(BSONString("hello")))
    }
    "pop items off an array" in {
      Await.result(TestDB.UpdateTestColl.execute(update("8").ops($pop("orig", PopPositions.First))), timeout).n mustEqual 1
      getDoc("8").get("orig") mustEqual Some(BSONArray(2, 3, 4))
      Await.result(TestDB.UpdateTestColl.execute(update("8").ops($pop("orig", PopPositions.Last))), timeout).n mustEqual 1
      getDoc("8").get("orig") mustEqual Some(BSONArray(2, 3))
    }
    "select multiple items to update" in {
      Await.result(TestDB.UpdateTestColl.execute(update("k" -> "v").ops($set("k1", "v1")).multi(true)), timeout).n mustEqual 4
    }

  }

}
