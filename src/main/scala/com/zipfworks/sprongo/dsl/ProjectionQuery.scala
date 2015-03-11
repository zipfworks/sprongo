package com.zipfworks.sprongo.dsl

import reactivemongo.api.QueryOpts
import reactivemongo.bson.BSONDocument

case class ProjectionQuery(
  query: BSONDocument = BSONDocument(),
  projection: BSONDocument = BSONDocument(),
  options: QueryOpts = QueryOpts(),
  sort: BSONDocument = BSONDocument(),
  limit: Int = -1,
  skip:Int = -1,
  stopOnError: Boolean = false
                            )
