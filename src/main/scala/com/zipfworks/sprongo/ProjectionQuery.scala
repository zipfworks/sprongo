package com.zipfworks.sprongo

import reactivemongo.bson.BSONDocument
import reactivemongo.api.QueryOpts

case class ProjectionQuery(
  query: BSONDocument = BSONDocument(),
  projection: BSONDocument = BSONDocument(),
  options: QueryOpts = QueryOpts(),
  sort: BSONDocument = BSONDocument(),
  limit: Int = -1,
  skip:Int = -1,
  stopOnError: Boolean = false
                            )
