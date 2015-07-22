package com.zipfworks.sprongo

import com.zipfworks.sprongo.commands.BSONDistinctCommand._
import play.api.libs.iteratee.Enumerator
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson._
import reactivemongo.api.Cursor
import scala.concurrent.{Future, ExecutionContext}
import spray.json._
import ExtendedJsonProtocol._
import reactivemongo.api.DefaultDB
import SprongoDSL._

class CollectionDAO[M <: Model](collectionName: String)(implicit ec: ExecutionContext, jsFormat: RootJsonFormat[M], db: DefaultDB) {

  //stored here if you just wanna use the raw reactivemongo collection
  val collection: BSONCollection = db(collectionName)

  //defaults
  private val defaultQueryDoc = BSONDocument()

  private implicit val docReader = new BSONDocumentReader[M] {
    def read(bson: BSONDocument) = {
      JsonBsonConverter.bdocToJsObject(bson).convertTo[M]
    }
  }

  private implicit val docWriter = new BSONDocumentWriter[M] {
    override def write(t: M): BSONDocument = {
      JsonBsonConverter.jsObjToBdoc(t.toJson.asJsObject)
    }
  }

  private implicit val mapDocReader = new BSONDocumentReader[Map[String, JsValue]] {
    override def read(bson: BSONDocument): Map[String, JsValue] = {
      JsonBsonConverter.bdocToJsObject(bson).convertTo[Map[String, JsValue]]
    }
  }

  def count(query: BSONDocument = defaultQueryDoc): Future[Int] = {
    collection.count(Some(query))
  }

  def distinct(field: String, query: BSONDocument = defaultQueryDoc): Future[BSONArray] = {
    import com.zipfworks.sprongo.commands.BSONDistinctCommand.Implicits._
    collection.runCommand(Distinct(field, query))
  }
  /**********************************************************************************
    * Creates
    *********************************************************************************/
  def exec(c: CreateQuery): Future[WriteResult] = {
    collection.insert(c.document)
  }

  /**********************************************************************************
    * Updates
    *********************************************************************************/
  def exec(u: UpdateQuery): Future[WriteResult] = {
    collection.update(
      selector = u.selector,
      update = u.update,
      upsert = u.upsert,
      multi = u.multi)
  }

  def exec(u: UpdateModelQuery[M]): Future[WriteResult] = {
    collection.update(
      selector = BSONDocument("_id" -> u.m.id),
      update = JsonBsonConverter.jsObjToBdoc(u.m.toJson.asJsObject),
      upsert = u.upsert,
      multi = u.multi
    )
  }

  /**********************************************************************************
    * Deletes
    *********************************************************************************/
  def exec(d: DeleteQuery): Future[WriteResult] = {
    collection.remove(
      query = d.s,
      firstMatchOnly = d.firstMatchOnly
    )
  }

  def exec(d: DeleteModelQuery[M]): Future[WriteResult] = {
    collection.remove(
      query = BSONDocument("_id" -> d.m.id),
      firstMatchOnly = d.firstMatchOnly
    )
  }


  /**********************************************************************************
   * Reads
   *********************************************************************************/
  private def getCursor(r: ReadQuery): Cursor[M] = {
    collection
      .find(r.sel)
      .sort(r.sort)
      .options(r.opts)
      .cursor[M](r.rp)
  }

  private def getCursor(r: ReadProjectionQuery): Cursor[Map[String, JsValue]] = {
    collection
      .find(r.q.sel, r.p)
      .sort(r.q.sort)
      .options(r.q.opts)
      .cursor[Map[String, JsValue]](r.q.rp)
  }

  def exec(r: ReadQuery): Enumerator[M] = {
    val cursor = getCursor(r)
    r.limit match {
      case None    => cursor.enumerate()
      case Some(l) => cursor.enumerate(maxDocs = l)
    }
  }

  def exec(r: ReadBulkQuery): Enumerator[Iterator[M]] = {
    r.q.limit match {
      case None    => getCursor(r.q).enumerateBulks()
      case Some(l) => getCursor(r.q).enumerateBulks(maxDocs = l)
    }
  }

  def exec(r: ReadProjectionQuery): Enumerator[Map[String, JsValue]] = {
    r.q.limit match {
      case None    => getCursor(r).enumerate()
      case Some(l) => getCursor(r).enumerate(maxDocs = l)
    }
  }

  def exec(r: ReadProjectionListQuery): Future[List[Map[String, JsValue]]] = {
    r.r.q.limit match {
      case None    => getCursor(r.r).collect[List]()
      case Some(l) => getCursor(r.r).collect[List](upTo = l)
    }
  }

  def exec(r: ReadProjectionBulkQuery): Enumerator[Iterator[Map[String, JsValue]]] = {
    r.r.q.limit match {
      case None    => getCursor(r.r).enumerateBulks()
      case Some(l) => getCursor(r.r).enumerateBulks(maxDocs = l)
    }
  }

  def exec(r: ReadListQuery): Future[List[M]] = {
    val cursor = getCursor(r.q)
    r.q.limit match {
      case None    => cursor.collect[List]()
      case Some(l) => cursor.collect[List](upTo = l)
    }
  }

  def exec(r: ReadOneQuery): Future[Option[M]] = {
    collection
      .find(r.q.sel)
      .sort(r.q.sort)
      .options(r.q.opts)
      .one[M](readPreference = r.q.rp)
  }

  def exec(r: ReadOneProjectionQuery): Future[Option[Map[String, JsValue]]] = {
    collection
      .find(r.q.sel, r.projection)
      .sort(r.q.sort)
      .options(r.q.opts)
      .one[Map[String, JsValue]](readPreference = r.q.rp)
  }

}



