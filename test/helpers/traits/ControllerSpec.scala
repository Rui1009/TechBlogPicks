package helpers.traits

import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import play.api.http.{ContentTypeOf, ContentTypes, Writeable}
import play.api.mvc.{AnyContentAsEmpty, Result, Codec}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

trait ControllerSpec extends UnitSpec with HasApplication {
  val badRequestError = "BadRequestError"

  val internalServerError = "InternalServerError"

  val urlError = "\ndomains.RegexError: PostUrl don't match pattern"

  def emptyStringError(className: String) =
    s"\ndomains.EmptyStringError: $className is empty string"

  def negativeNumberError(className: String) =
    s"\ndomains.NegativeNumberError: $className is negative number"

  case class Response[T](data: T)
  case class ErrorResponse(message: String)

  def decodeRes[T: Decoder](res: Future[Result]): Either[Error, Response[T]] =
    decode[Response[T]](contentAsString(res))

  def unsafeDecodeRes[T: Decoder](res: Future[Result]): Response[T] =
    decodeRes(res).unsafeGet

  def decodeERes(res: Future[Result]): Either[Error, ErrorResponse] =
    decode[ErrorResponse](contentAsString(res))

  def unsafeDecodeERes[T: Decoder](res: Future[Result]): ErrorResponse =
    decode[ErrorResponse](contentAsString(res)).unsafeGet

  case class Request[T: Writeable](fkReq: FakeRequest[T]) {
    import Request._

    def exec: Option[Future[Result]] = route(app, fkReq)

    def unsafeExec: Future[Result] = exec.get

    def withJsonBody(json: Json): Request[Json] =
      Request(fkReq.withBody[Json](json))

    def withJsonBody[B: Encoder](body: B): Request[Json] =
      withJsonBody(body.asJson)

  }

  object Request {
    def apply(method: String, path: String): Request[AnyContentAsEmpty.type] =
      Request(FakeRequest(method, path))

    def get(path: String): Request[AnyContentAsEmpty.type] = Request(GET, path)

    def post(path: String): Request[AnyContentAsEmpty.type] =
      Request(POST, path)

    def put(path: String): Request[AnyContentAsEmpty.type] = Request(PUT, path)

    def delete(path: String): Request[AnyContentAsEmpty.type] =
      Request(DELETE, path)

    private val defaultPrinter = Printer.noSpaces

    implicit val contentTypeOf_Json: ContentTypeOf[Json] =
      ContentTypeOf(Some(ContentTypes.JSON))

    implicit def writableOf_Json(implicit
      codec: Codec,
      printer: Printer = defaultPrinter
    ): Writeable[Json] = Writeable(a => codec.encode(a.printWith(printer)))
  }
}
