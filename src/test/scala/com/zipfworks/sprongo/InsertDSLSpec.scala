package com.zipfworks.sprongo

import com.zipfworks.sprongo.macros.InsertDSL._
import org.joda.time.DateTime
import org.specs2.mutable._
import reactivemongo.bson.BSONDocument

import scala.concurrent.Await

class InsertDSLSpec extends Specification {
  import Common._

  sequential

  val test1 = TestEntry("blah1", 1, 1, DateTime.now)
  val test2 = TestEntry("blah2", 2, 2, DateTime.now)
  val test3 = TestEntry("blah3", 3, 3, DateTime.now)
  val test4 = TestEntry("blah4", 4, 4, DateTime.now)
  val test5 = TestEntry("blah5", 5, 5, DateTime.now)
  val bulk = Seq.range(1, 6).map(i => TestEntry("bulk" + i, i, i.toLong, DateTime.now))
  val testDoc =  BSONDocument("testField" -> 990000)

  //clear out the collection before we start test
  step(Await.ready(TestDB.InsertTestColl.drop(), timeout))

  "InsertDSL" should {
    "write 5 single models with success" in {
      Await.result(TestDB.InsertTestColl.execute(insert(test1)), timeout).ok mustEqual true
      Await.result(TestDB.InsertTestColl.execute(insert(test2)), timeout).ok mustEqual true
      Await.result(TestDB.InsertTestColl.execute(insert(test3)), timeout).ok mustEqual true
      Await.result(TestDB.InsertTestColl.execute(insert(test4)), timeout).ok mustEqual true
      Await.result(TestDB.InsertTestColl.execute(insert(test5)), timeout).ok mustEqual true
    }
    "write a bulk of 5 models with success" in {
      Await.result(TestDB.InsertTestColl.execute(insert(bulk)), timeout) mustEqual 5
    }
    "write a generic BSONDocument with success" in {
      Await.result(TestDB.InsertTestColl.execute(insert(testDoc)), timeout).ok mustEqual true
    }
  }

  //clean up the collection when we finish up
  step(Await.ready(TestDB.InsertTestColl.drop(), timeout))
}
