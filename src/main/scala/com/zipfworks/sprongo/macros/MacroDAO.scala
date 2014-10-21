package com.zipfworks.sprongo.macros

import reactivemongo.api.{FailoverStrategy, DB}
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter}

class MacroDAO[T](coll_name: String)(implicit db: DB, writer: BSONDocumentWriter[T], reader: BSONDocumentReader[T])
  extends BSONCollection(db, coll_name, FailoverStrategy()) {

}
