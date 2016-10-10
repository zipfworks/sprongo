## Sprongo - ReactiveMongo integration for Spray ##

Sprongo provides [ReactiveMongo](http://reactivemongo.org/) integration
with [Spray](http://spray.io/) which makes building a REST API easier.

This library helps power the API at [https://dealspotr.com/](https://dealspotr.com/)

## Building the Library ##

To use Sprongo, you'll need to build the project and publish it locally
with SBT:

    $ git clone https://github.com/zipfworks/sprongo.git
    $ cd sprongo
    $ sbt
    > publish-local

## Using the Library - QuickStart ##

In order to use Sprongo in your project, first add it as a dependency in
your project's build.sbt:

    libraryDependencies += "com.zipfworks" %% "sprongo" % "1.1.1-SNAPSHOT"

In your project, create a class for the database driver and, classes for
your models, and case classes for your JSON marshalling. You may pass an
optional actor system as the third argument to SprongoDB.
 Example:

```scala
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

class DBDriver(dbUrls: Seq[String], dbName: String, system: Option[ActorSystem]) extends SprongoDB(dbUrls, dbName, system) {
  object Widgets extends CollectionDAO[Widget]("widgets")
}
```

Once your driver and models are defined, you can create an instance of
the driver (or use a companion object) and access the DAO methods for your
collections.

```scala
val dbName = "sprongo-example-db"
val dbUrls = Seq("localhost")
val sys = ActorSystem("sprongo-example")
val db = new DBDriver(dbUrls, dbName, Some(sys))


// Use the DSL to query the db
import com.zipfworks.sprongo.SprongoDSL._

// Read a single record
val widget = db.Widgets.exec(read.id("some-id"))

// Read a list
val cmd = read.selector(BSONDocument("$gt" -> 1)).asList
val widgets = db.Widgets.exec(cmd))

// Create a record
val newWidget = Widget(
  name        = "foo",
  quantity    = 1,
  price       = 4.95,
  description = Some("bar"),
  id          = UUID.randomUUID().toString
)
val cmd = create.model(newWidget)
db.Widgets.exec(cmd)

// Update a record
val cmd = update.model(newWidget)
db.Widgets.exec(cmd))

// Partially update a record
val cmd = update.id(newWidget.id).update(set("name" -> "foo2"), ...)
db.Widgets.exec(cmd))

// Delete a record
db.Widgets.exec(delete.model(newWidget))
db.Widgets.exec(delete.id(newWidget.id))

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

