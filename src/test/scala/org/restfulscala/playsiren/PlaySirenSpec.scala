package org.restfulscala.playsiren

import com.yetu.siren.model.{Entity, Link}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play._
import play.api.libs.json.JsObject
import play.api.mvc._
import play.api.test.FakeRequest
import play.twirl.api.Html

class PlaySirenSpec extends PlaySpec with Results with ScalaFutures {

  import org.scalatest.prop.TableDrivenPropertyChecks._
  import play.api.test.Helpers._

  "sirenJsonContentType" must {
    "return the Siren media type plus encoding" in {
      val data = Table(
        "encoding" -> "contentType",
        play.api.mvc.Codec.utf_8 -> "application/vnd.siren+json; charset=utf-8",
        play.api.mvc.Codec.iso_8859_1 -> "application/vnd.siren+json; charset=iso-8859-1"
      )
      forAll(data) { (encoding, contentType) =>
        assert(sirenJsonContentType(encoding) === contentType)
      }
    }
  }
  "Writeable[RootEntity]" must {
    "allow responding with Siren RootEntity values" in {
      val entity = Entity.RootEntity(links = Some(List(Link(List("self"), "http://example.com"))))
      object controller extends Controller {
        def index() = Action {
          Ok(entity)
        }
      }
      val result = controller.index()(FakeRequest())
      status(result) mustEqual 200
      header("Content-type", result) mustEqual Some("application/vnd.siren+json; charset=utf-8")
      contentAsString(result) must include ("http://example.com")
    }
  }
  "AcceptsSirenJson request extractor" must {
    "be usable for content negotation" in {
      val data = Table(
        ("Accept",                                             "Status Code",  "Content-type"),
        ("application/vnd.siren+json",                          200,            Some("application/vnd.siren+json; charset=utf-8")),
        ("application/json, application/vnd.siren+json; q=0.8", 200,            Some("application/json; charset=utf-8")),
        ("*/*",                                                 200,            Some("text/html; charset=utf-8")),
        ("application/hal+json",                                406,            None)
      )
      object controller extends Controller {
        def index() = Action { implicit req =>
          render {
            case Accepts.Html() => Ok(Html("<html></html>"))
            case AcceptsSirenJson() => Ok(Entity.RootEntity())
            case Accepts.Json() => Ok(JsObject(Seq.empty))
          }
        }
      }
      forAll(data) { (acceptHeader, responseStatus, contentType) =>
        val result = controller.index()(FakeRequest().withHeaders("Accept" -> acceptHeader))
        status(result) mustEqual responseStatus
        header("Content-type", result) mustEqual contentType
      }
    }
  }

}
