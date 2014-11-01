package com.zipfworks.sprongo

import com.zipfworks.sprongo.macros.RemoveDSL._
import org.specs2.mutable._
import reactivemongo.bson.{BSONString, BSONDocumentWriter, BSONDocument}
import scala.concurrent.Await

class RemoveDSLSpec extends Specification {
  import Common._

  sequential

  private val test_id = "some-test-id"

  //clear out and prime the collection with a document
  step({
    Await.ready(TestDB.RemoveTestColl.drop(), timeout)
    Await.ready(TestDB.RemoveTestColl.insert(BSONDocument("_id" -> test_id)), timeout)
  })

  "RemoveDSL" should {
    "remove a single document" in {
      Await.result(TestDB.RemoveTestColl.execute(remove(BSONDocument("_id" -> test_id))), timeout).n mustEqual 1
    }
  }

  //clean up the collection
//  step(Await.ready(TestDB.RemoveTestColl.drop(), timeout))
}
