package org.restfulscala

/**
 * Provides support for integrating siren-scala with the Play framework.
 *
 * ==Usage==
 * By importing this package, several helpful typeclass instances are put into scope. These
 * allow you to respond with Siren `RootEntity` values as well as with values of types that
 * can be converted to a Siren `RootEntity`.
 *
 * Moreover, this package provides a request extractor, `AcceptsSirenJson`, for Play's
 * content negotation support, allowing you to respond appropriately to requests with
 * an `Accept` header whose media range demands a Siren JSON representation.
 */
package object playsiren {

  import play.api.http.{ContentTypes, ContentTypeOf, Writeable}
  import play.api.libs.json.Json
  import play.api.mvc.Codec
  import play.api.mvc.Accepting

  import com.yetu.siren.model.Entity.RootEntity
  import com.yetu.siren.{Siren, SirenRootEntityWriter}
  import com.yetu.siren.json.playjson.PlayJsonSirenFormat._

  /**
   * The Siren JSON media type.
   */
  val sirenJsonMediaType = "application/vnd.siren+json"

  /**
   * Siren JSON content type including charset.
   */
  def sirenJsonContentType(implicit codec: Codec): String = ContentTypes withCharset sirenJsonMediaType

  /**
   * The content type for Siren `RootEntity` values.
   */
  implicit def contentTypeOfSirenJson(implicit codec: Codec): ContentTypeOf[RootEntity] =
    ContentTypeOf[RootEntity](Some(sirenJsonContentType))

  /**
   * `Writeable` for Siren `RootEntity` values - Siren JSON.
   */
  implicit def writableOfSirenJson(implicit codec: Codec): Writeable[RootEntity] =
    Writeable(e =>  codec.encode(Json.toJson(e).toString()))

  /**
    * Media type extractor for Siren JSON.
   */
  val AcceptsSirenJson = Accepting(sirenJsonMediaType)

}
