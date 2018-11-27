package endpoints.openapi

import endpoints.openapi.model._
import endpoints.{algebra, macros, openapi}
import org.scalatest.{Matchers, WordSpec}

class ReferencedSchemaTest extends WordSpec with Matchers {

  sealed trait Storage
  case class StorageLibrary(room: String, shelf: Int) extends Storage
  case class StorageOnline(link: String) extends Storage

  case class Book(id: Int, title: String, author: String, isbnCodes: List[String], storage: Storage,
                  sets:List[Int])

  case class Storages(onlineStorages:Map[String, Storage])

  object Fixtures extends Fixtures with openapi.Endpoints with openapi.JsonSchemaEntities {

    def openApi: OpenApi = openApi(
      Info(title = "TestFixturesOpenApi", version = "0.0.0")
    )(Fixtures.listBooks, Fixtures.postBook, Fixtures.listStorages)
  }

  trait Fixtures extends algebra.Endpoints with algebra.JsonSchemaEntities with macros.JsonSchemas {

//    implicit private val storageSchema: JsonSchema[Storage] = genericJsonSchema[Storage]
//
//    implicit private val storageLibSchema: JsonSchema[StorageLibrary] = genericJsonSchema[StorageLibrary]
//
//    implicit private val storageOLSchema: JsonSchema[StorageOnline] = genericJsonSchema[StorageOnline]

    implicit private lazy val schemaStorage: JsonSchema[Storage] =
      withDiscriminator(genericJsonSchema[Storage].asInstanceOf[Tagged[Storage]], "storageType")

    implicit private lazy val storagesBook: JsonSchema[Storages] = genericJsonSchema[Storages]


    implicit private lazy val schemaBook: JsonSchema[Book] = genericJsonSchema[Book]

    val listBooks = endpoint(get(path / "books"), jsonResponse[List[Book]](Some("Books list")), tags = List("Books"))

    val postBook = endpoint(post(path / "books", jsonRequest[Book](docs = Some("Books list"))), emptyResponse(), tags = List("Books"))

    val listStorages = endpoint(get(path / "storages"), jsonResponse[List[Storages]](Some("Storage list")), tags = List("Storage"))

  }

  "OpenApi" should {

    "produce referenced schema" in {

      import io.circe.syntax._

      Fixtures.openApi.asJson.spaces2 shouldBe
        """{
          |  "components" : {
          |    "schemas" : {
          |      "endpoints.openapi.ReferencedSchemaTest.Book" : {
          |        "required" : [
          |          "id",
          |          "title",
          |          "author",
          |          "isbnCodes",
          |          "storage",
          |          "sets"
          |        ],
          |        "type" : "object",
          |        "properties" : {
          |          "id" : {
          |            "type" : "integer",
          |            "format" : "int32"
          |          },
          |          "title" : {
          |            "type" : "string"
          |          },
          |          "author" : {
          |            "type" : "string"
          |          },
          |          "isbnCodes" : {
          |            "type" : "array",
          |            "items" : {
          |              "type" : "string"
          |            }
          |          },
          |          "storage" : {
          |            "$ref" : "#/components/schemas/endpoints.openapi.ReferencedSchemaTest.Storage"
          |          },
          |          "sets" : {
          |            "type" : "array",
          |            "items" : {
          |              "type" : "integer",
          |              "format" : "int32"
          |            }
          |          }
          |        }
          |      },
          |      "endpoints.openapi.ReferencedSchemaTest.Storage" : {
          |        "oneOf" : [
          |          {
          |            "$ref" : "#/components/schemas/endpoints.openapi.ReferencedSchemaTest.StorageLibrary"
          |          },
          |          {
          |            "$ref" : "#/components/schemas/endpoints.openapi.ReferencedSchemaTest.StorageOnline"
          |          }
          |        ],
          |        "discriminator" : {
          |          "propertyName" : "storageType",
          |          "mapping" : {
          |            "StorageLibrary" : "#/components/schemas/endpoints.openapi.ReferencedSchemaTest.StorageLibrary",
          |            "StorageOnline" : "#/components/schemas/endpoints.openapi.ReferencedSchemaTest.StorageOnline"
          |          }
          |        }
          |      },
          |      "endpoints.openapi.ReferencedSchemaTest.StorageLibrary" : {
          |        "allOf" : [
          |          {
          |            "$ref" : "#/components/schemas/endpoints.openapi.ReferencedSchemaTest.Storage"
          |          },
          |          {
          |            "required" : [
          |              "storageType",
          |              "room",
          |              "shelf"
          |            ],
          |            "type" : "object",
          |            "properties" : {
          |              "storageType" : {
          |                "type" : "string"
          |              },
          |              "room" : {
          |                "type" : "string"
          |              },
          |              "shelf" : {
          |                "type" : "integer",
          |                "format" : "int32"
          |              }
          |            }
          |          }
          |        ]
          |      },
          |      "endpoints.openapi.ReferencedSchemaTest.StorageOnline" : {
          |        "allOf" : [
          |          {
          |            "$ref" : "#/components/schemas/endpoints.openapi.ReferencedSchemaTest.Storage"
          |          },
          |          {
          |            "required" : [
          |              "storageType",
          |              "link"
          |            ],
          |            "type" : "object",
          |            "properties" : {
          |              "storageType" : {
          |                "type" : "string"
          |              },
          |              "link" : {
          |                "type" : "string"
          |              }
          |            }
          |          }
          |        ]
          |      },
          |      "endpoints.openapi.ReferencedSchemaTest.Storages" : {
          |        "required" : [
          |          "onlineStorages"
          |        ],
          |        "type" : "object",
          |        "properties" : {
          |          "onlineStorages" : {
          |            "type" : "object",
          |            "properties" : {
          |            },
          |            "additionalProperties" : {
          |              "$ref" : "#/components/schemas/endpoints.openapi.ReferencedSchemaTest.Storage"
          |            }
          |          }
          |        }
          |      }
          |    }
          |  },
          |  "openapi" : "3.0.0",
          |  "info" : {
          |    "title" : "TestFixturesOpenApi",
          |    "version" : "0.0.0"
          |  },
          |  "paths" : {
          |    "/storages" : {
          |      "get" : {
          |        "responses" : {
          |          "200" : {
          |            "description" : "Storage list",
          |            "content" : {
          |              "application/json" : {
          |                "schema" : {
          |                  "type" : "array",
          |                  "items" : {
          |                    "$ref" : "#/components/schemas/endpoints.openapi.ReferencedSchemaTest.Storages"
          |                  }
          |                }
          |              }
          |            }
          |          }
          |        },
          |        "tags" : [
          |          "Storage"
          |        ]
          |      }
          |    },
          |    "/books" : {
          |      "get" : {
          |        "responses" : {
          |          "200" : {
          |            "description" : "Books list",
          |            "content" : {
          |              "application/json" : {
          |                "schema" : {
          |                  "type" : "array",
          |                  "items" : {
          |                    "$ref" : "#/components/schemas/endpoints.openapi.ReferencedSchemaTest.Book"
          |                  }
          |                }
          |              }
          |            }
          |          }
          |        },
          |        "tags" : [
          |          "Books"
          |        ]
          |      },
          |      "post" : {
          |        "responses" : {
          |          "200" : {
          |            "description" : ""
          |          }
          |        },
          |        "requestBody" : {
          |          "description" : "Books list",
          |          "content" : {
          |            "application/json" : {
          |              "schema" : {
          |                "$ref" : "#/components/schemas/endpoints.openapi.ReferencedSchemaTest.Book"
          |              }
          |            }
          |          }
          |        },
          |        "tags" : [
          |          "Books"
          |        ]
          |      }
          |    }
          |  }
          |}""".stripMargin
    }
  }
}
