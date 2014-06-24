package com.zipfworks.sprongo

import play.api.libs.iteratee.Enumerator
import reactivemongo.bson._
import reactivemongo.api.Cursor
import scala.concurrent.{Future, ExecutionContext}
import spray.json._
import reactivemongo.core.commands.{LastError, Count}
import com.zipfworks.sprongo.commands.Distinct
import ExtendedJsonProtocol._
import reactivemongo.api.QueryOpts
import reactivemongo.api.DefaultDB
import reactivemongo.api.collections.default.BSONCollection
import SprongoDSL._

class CollectionDAO[M <: Model](collectionName: String)(implicit ec: ExecutionContext, jsFormat: RootJsonFormat[M], db: DefaultDB) {

  //stored here if you just wanna use the raw reactivemongo collection
  val collection: BSONCollection = db(collectionName)

  //defaults
  private val defaultQueryDoc = BSONDocument()
  private val defaultQueryOpts = QueryOpts().slaveOk
  private val defaultSortDoc = BSONDocument()

  private implicit val docReader = new BSONDocumentReader[M] {
    def read(bson: BSONDocument) = {
      JsonBsonConverter.bdocToJsObject(bson).convertTo[M]
    }
  }

  private implicit val mapDocReader = new BSONDocumentReader[Map[String, JsValue]] {
    override def read(bson: BSONDocument): Map[String, JsValue] = {
      JsonBsonConverter.bdocToJsObject(bson).convertTo[Map[String, JsValue]]
    }
  }

  def create(m: M) = {
    val bdoc = JsonBsonConverter.jsObjToBdoc(m.toJson.asJsObject)
    collection.insert(bdoc)
  }

  /**
   * read WITH a projection
   * @param pq ProjectionQuery -> wrapper for a query and projection
   * @return A Map[String, JsValue] map because it can't get all the way to a case class
   */
  def read(pq: ProjectionQuery): Cursor[Map[String, JsValue]] = {
    collection.find(pq.query, pq.projection).options(pq.options).sort(pq.sort).cursor[Map[String, JsValue]]
  }

  /**
   * read WITHOUT a projection, see above
   * @param q BSONDocument
   * @param options QueryOpts
   * @param sort BSONDocument
   * @return The actual case class
   */
  def read(q: BSONDocument = defaultQueryDoc, options: QueryOpts = defaultQueryOpts, sort: BSONDocument = defaultSortDoc): Cursor[M] = {
    collection.find(q).options(options).sort(sort).cursor[M]
  }

  /**
   * Returns the Future of the object if id exists, None otherwise
   * @param id ID of the model
   * @return Future[ Option[None] ]
   */
  def readById(id: String): Future[Option[M]] = {
    val q = BSONDocument("_id" -> id)
    read(q).collect[List](1).map(list => if(list.isEmpty) None else Some(list.head))
  }

  def readOne(q: BSONDocument = defaultQueryDoc): Future[Option[M]] = readList(q).map {
    case Nil     => None
    case x :: xs => Some(x)
  }

  private def addSkip(skip: Int, options: QueryOpts): QueryOpts = {
    if(skip < 0) options else options.skip(skip)
  }

  def readList(q: BSONDocument = defaultQueryDoc, options: QueryOpts = defaultQueryOpts,
               sort: BSONDocument = defaultSortDoc, limit: Int = -1, skip: Int = -1,
               stopOnError: Boolean = false): Future[List[M]] = {
    val optionsWithSkip = addSkip(skip, options)

    if(limit < 0){
      read(q, optionsWithSkip, sort).collect[List](stopOnError = stopOnError)
    } else {
      read(q, optionsWithSkip, sort).collect[List](upTo = limit, stopOnError = stopOnError)
    }
  }

  def readList(pq: ProjectionQuery): Future[List[Map[String, JsValue]]] = {
    val options = if(pq.skip > 0) addSkip(pq.skip, pq.options) else pq.options
    val withCorrectOptions = pq.copy(options = options)

    if(pq.limit > 0){
      read(withCorrectOptions).collect[List](upTo = pq.limit, stopOnError = pq.stopOnError)
    } else {
      read(withCorrectOptions).collect[List](stopOnError = pq.stopOnError)
    }
  }

  def readListByIds(idList: List[String]): Future[List[M]] = {
    val query = BSONDocument("_id" -> BSONDocument("$in" -> BSONArray(idList.map(id => BSONString(id)))))
    read(query).collect[List]()
  }

  def update(m: M) = {
    val selector = BSONDocument("_id" -> m.id)
    val update = JsonBsonConverter.jsObjToBdoc(m.toJson.asJsObject)
    collection.update(selector, update)
  }

  def updatePart(m: M, query: BSONDocument) = {
    val selector = BSONDocument("_id" -> m.id)
    val update = BSONDocument("$set" -> query)
    collection.update(selector, update)
  }

  def updatePart[T1](selector: BSONDocument, field: String, value: T1)(implicit write: JsonWriter[T1]): Future[LastError] = {
    val bsValue = JsonBsonConverter.jsValueToBsonVal(value.toJson)
    val update = BSONDocument("$set" -> BSONDocument(field -> bsValue))
    collection.update(selector, update)
  }

  def updatePart[T1](id: String, field: String, value: T1)(implicit writer: JsonWriter[T1]): Future[LastError] = {
    val selector = BSONDocument("_id" -> id)
    updatePart(selector, field, value)
  }


  /**
   * http://docs.mongodb.org/manual/reference/operator/update/push/
   * @param id: String - id of document
   * @param field: String - field name of array
   * @param value: what to push onto the array
   * @return
   */
  def push[T1](id: String, field: String, value: T1)(implicit writer: JsonWriter[T1]): Future[LastError] = {
    val selector = BSONDocument("_id" -> id)
    val bsValue = JsonBsonConverter.jsValueToBsonVal(value.toJson)
    val update = BSONDocument("$push" -> BSONDocument(field -> bsValue))
    collection.update(selector, update)
  }

  def pull(id: String, field: String, query: BSONDocument): Future[LastError] = {
    val selector = BSONDocument("_id" -> id)
    val update = BSONDocument("$pull" -> BSONDocument(field -> query))
    collection.update(selector, update)
  }

  def unset(selector: BSONDocument, fields: List[String]): Future[LastError] = {
    val unsetOps = fields.foldLeft(BSONDocument())((doc, field) => doc.add(field -> ""))
    val update = BSONDocument("$unset" -> unsetOps)
    collection.update(selector, update)
  }

  def increment(id: String, field: String, amount: Int): Future[LastError] = {
    val selector = BSONDocument("_id" -> id)
    val update = BSONDocument("$inc" -> BSONDocument(field -> amount))
    collection.update(selector, update)
  }

  def delete(m: M) = {
    val selector = BSONDocument("_id" -> m.id)
    collection.remove(selector)
  }

  def deleteById(id: String): Future[LastError] = {
    collection.remove(BSONDocument("_id" -> id))
  }

  def count(query: BSONDocument = defaultQueryDoc): Future[Int] = {
    db.command(new Count(collectionName, Some(query)))
  }

  def distinct(field: String, query: BSONDocument = defaultQueryDoc): Future[BSONArray] = {
    db.command(new Distinct(collectionName, field, Some(query)))
  }

  def exec(u: UpdateQuery): Future[LastError] = {
    collection.update(
      selector = u.selector,
      update = u.update,
      upsert = u.upsert,
      multi = u.multi)
  }

  def exec(u: UpdateModelQuery[M]): Future[LastError] = {
    collection.update(
      selector = BSONDocument("_id" -> u.m.id),
      update = JsonBsonConverter.jsObjToBdoc(u.m.toJson.asJsObject),
      upsert = u.upsert,
      multi = u.multi
    )
  }

  def exec(d: DeleteQuery): Future[LastError] = {
    collection.remove(
      query = d.s,
      firstMatchOnly = d.firstMatchOnly
    )
  }

  def exec(d: DeleteModelQuery[M]): Future[LastError] = {
    collection.remove(
      query = BSONDocument("_id" -> d.m.id),
      firstMatchOnly = d.firstMatchOnly
    )
  }

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
      .find(r.q.sel, BSONDocument(r.fields: _*))
      .sort(r.q.sort)
      .options(r.q.opts)
      .one[Map[String, JsValue]](readPreference = r.q.rp)
  }

}



