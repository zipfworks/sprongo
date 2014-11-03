package com.zipfworks.sprongo

import com.zipfworks.sprongo.macros.RemoveDSL._
import org.joda.time.DateTime
import org.specs2.mutable._
import reactivemongo.bson.BSONDocument
import scala.concurrent.Await

class RemoveDSLSpec extends Specification {
  import Common._
  import com.zipfworks.sprongo.macros.SelWriters._

  sequential

  private val test_id = "some-test-id"
  private val test_model = TestEntry("1", 1, 1, DateTime.now)
  private val test_doc = BSONDocument("key" -> "value")
  private val test_doc2 = BSONDocument("key2" -> "value2", "key3" -> "value3")

  //clear out and prime the collection with a document
  step({
    Await.ready(TestDB.RemoveTestColl.drop(), timeout)
    Await.ready(TestDB.RemoveTestColl.insert(BSONDocument("_id" -> test_id)), timeout)
    Await.ready(TestDB.RemoveTestColl.insert(test_model), timeout)
    Await.ready(TestDB.RemoveTestColl.insert(test_doc), timeout)
    Await.ready(TestDB.RemoveTestColl.insert(test_doc), timeout)
    Await.ready(TestDB.RemoveTestColl.insert(test_doc), timeout)
    Await.ready(TestDB.RemoveTestColl.insert(test_doc2), timeout)
    Await.ready(TestDB.RemoveTestColl.insert(test_doc2), timeout)
    Await.ready(TestDB.RemoveTestColl.insert(test_doc2), timeout)
  })

  "RemoveDSL" should {
    "remove a single model" in {
      Await.result(TestDB.RemoveTestColl.execute(remove(test_model)), timeout).n mustEqual 1
    }
    "remove a single document" in {
      Await.result(TestDB.RemoveTestColl.execute(remove(test_id)), timeout).n mustEqual 1
    }
    "remove multiple documents with one criteria" in {
      Await.result(TestDB.RemoveTestColl.execute(remove("key" -> "value").multi(true)), timeout).n mustEqual 3
    }
    "remove multiple documents with more than one criteria" in {
      Await.result(TestDB.RemoveTestColl.execute(remove("key2" -> "value2", "key3" -> "value3").multi(true)), timeout).n mustEqual 3
    }
  }

  //clean up the collection
  step(Await.ready(TestDB.RemoveTestColl.drop(), timeout))
}
