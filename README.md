## Sprongo - ReactiveMongo integration for Spray ##

Sprongo provides [ReactiveMongo](http://reactivemongo.org/) integration
with [Spray](http://spray.io/) which makes building a REST API easier.

## Building the Library ##

To use Sprongo, you'll need to build the project and publish it locally
with SBT:

    $ git clone https://github.com/zipfworks/sprongo.git
    $ cd sprongo
    $ sbt
    > publish-local

## Using the Library ##

In order to use Sprongo in your project, first add it as a dependency in
your project's build.sbt:

    libraryDependencies += "com.zipfworks" %% "sprongo" % "1.0.4-SNAPSHOT"

In your project, create a class for the database driver and, classes for
your models, and case classes for your JSON marshalling. You may pass an
optional actor system as the third argument to SprongoDB.
 Example:

```scala
package com.zipfworks.sprongo-example

import com.zipfworks.sprongo.{CollectionDAO, SprongoDB, Model, ExtendedJsonProtocol}
import scala.concurrent.ExecutionContext.Implicits.global

case class Widget (
  name:         String,
  quantity:     Int,
  price:        Float,
  description:  Option[String] = None,
  id:           String
) extends Model

object Widget extends ExtendedJsonProtocol {
  implicit val widgetJsonFormat = jsonFormat5(Widget.apply)
}

class DBDriver(dbUrls: Seq[String], dbName: String, system: Option[ActorSystem] = None) extends SprongoDB(dbUrls, dbName, system) {
  object Widgets extends CollectionDAO[Widget]("widgets")
}
```

Once your driver and models are defined, you can create an instance of
the driver (or use a companion object) and access the DAO methods for your
collections.

```scala

val db = new DBDriver(Seq("localhost:27017"), "sprongo-example-db")

// Read a single record
val widget = db.Widgets.findById("some-id")

// Read a list
val listQuery = BSONDocument(
  "quantity" -> BSONDocument("$gt" -> 1)
)
val widgets = db.Widgets.read(listQuery)

// Create a record
val newWidget = Widget(
  name        = "foo",
  quantity    = 1,
  price       = 4.95,
  description = Some("bar"),
  id          = UUID.randomUUID().toString
)
db.Widgets.create(newWidget)

// Update a record
db.Widgets.update(newWidget)

// Delete a record
val deleteQuery = BSONDocument("id" -> "some-id")
db.Widgets.delete(deleteQuery)

```

Stay tuned for more details and an example project.

## Publishing ##

From sbt, enter `publish-local` to publish locally. This will publish
the code to your local repo for ivy, which is in the path by default.

If publishing to a remote Nexus repository, create a credentials file
in ~/.sbt/.credentials with the following format and enter `publish`
from sbt:

    realm=Sonatype Nexus Repository Manager
    host=nexus.zipfworks.com
    user=<username>
    password=<password>

## License ##

This software is licensed under the Apache 2 license, quoted below.

Copyright © 2014 [ZipfWorks](http://www.zipfworks.com)

Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at

    [http://www.apache.org/licenses/LICENSE-2.0]

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.

