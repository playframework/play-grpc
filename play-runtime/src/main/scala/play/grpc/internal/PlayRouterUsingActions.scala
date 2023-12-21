/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */
package play.grpc.internal

import scala.collection.immutable
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.annotation.InternalApi
import org.apache.pekko.grpc.internal.GrpcProtocolNative
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.http.scaladsl.model.HttpHeader.ParsingResult
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString
import play.api.http.HttpChunk
import play.api.http.HttpChunk.Chunk
import play.api.http.HttpChunk.LastChunk
import play.api.http.HttpEntity.Chunked
import play.api.libs.streams.Accumulator
import play.api.mvc._
import play.api.routing.Router
import play.api.routing.Router.Routes

/**
 * Boiler plate needed for the generated Play routers allowing for adding a service implementation in a Play app,
 * inherited by the generated abstract service router (both Java and Scala) which is then implemented by the user.
 *
 * INTERNAL API
 */
@InternalApi abstract class PlayRouterUsingActions(
    system: ActorSystem,
    serviceName: String,
    parsers: PlayBodyParsers,
    actionBuilder: ActionBuilder[Request, AnyContent],
) extends play.api.routing.Router {

  private val prefix = s"/$serviceName"

  /**
   * INTERNAL API
   */
  @InternalApi
  protected def createHandler(serviceName: String, system: ActorSystem): RequestHeader => EssentialAction

  private val handler = createHandler(serviceName, system)

  // Scala API
  final override def routes: Routes = {
    case rh if rh.path.startsWith(prefix) ⇒ handler(rh)
  }

  final override def documentation: Seq[(String, String, String)] = Seq.empty

  /**
   * Registering a gRPC service under a custom prefix is not widely supported and strongly discouraged by the specification
   * so therefore not supported.
   */
  final override def withPrefix(prefix: String): Router =
    if (prefix == "/") this
    else
      throw new UnsupportedOperationException(
        "Prefixing gRPC services is not widely supported by clients, " +
          s"strongly discouraged by the specification and therefore not supported. " +
          s"Attempted to prefix with [$prefix], yet already default prefix known to be [${this.prefix}]. " +
          s"When binding gRPC routers the path in `routes` MUST BE `/`.",
      )

  def createStreamingAction(
      handler: HttpRequest => Future[HttpResponse],
  )(implicit ec: ExecutionContext): EssentialAction =
    actionBuilder.async(streamBodyParser) { req =>
      handler(playToPekkoRequestStream(req)).map(pekkoToPlayResp)
    }

  def createUnaryAction(handler: HttpRequest => Future[HttpResponse])(implicit ec: ExecutionContext): EssentialAction =
    actionBuilder.async(parsers.byteString) { req =>
      handler(playToPekkoRequest(req)).map(pekkoToPlayResp)
    }

  def streamBodyParser(implicit ec: ExecutionContext): BodyParser[Source[ByteString, _]] = BodyParser("stream") { _ =>
    Accumulator
      .source[ByteString]
      .map(Right.apply)
  }

  def playToPekkoRequest(request: Request[ByteString]): HttpRequest = {
    val entity =
      HttpEntity.Chunked.fromData(GrpcProtocolNative.contentType, chunks = Source.single(request.body))
    HttpRequest(
      method = HttpMethods.getForKey(request.method.toUpperCase).get,
      uri = Uri(request.uri),
      headers = playToPekkoHeaders(request),
      entity = entity,
      protocol = HttpProtocols.getForKey(request.version.toUpperCase).get,
    )
  }

  def playToPekkoRequestStream(request: Request[Source[ByteString, _]]): HttpRequest = {
    val entity =
      HttpEntity.Chunked.fromData(GrpcProtocolNative.contentType, chunks = request.body)
    HttpRequest(
      method = HttpMethods.getForKey(request.method.toUpperCase).get,
      uri = Uri(request.uri),
      headers = playToPekkoHeaders(request),
      entity = entity,
      protocol = HttpProtocols.getForKey(request.version.toUpperCase).get,
    )
  }

  def playToPekkoHeaders(req: Request[_]): immutable.Seq[HttpHeader] = {
    immutable.Seq(req.headers.headers: _*).map { h =>
      HttpHeader.parse(h._1, h._2) match {
        case ParsingResult.Ok(header, errors) => header
        case ParsingResult.Error(error)       => throw new Exception("header parsing")
      }
    }
  }

  def pekkoToPlayResp(pekkoResp: HttpResponse): Result = {
    val playEntity = pekkoResp.entity match {
      case HttpEntity.Chunked(ct, chunks) =>
        val playChunks: Source[HttpChunk, Any] = chunks.map {
          case HttpEntity.LastChunk(_, trailer) =>
            LastChunk(pekkoToPlayHeaders(trailer))
          case HttpEntity.Chunk(data, ext) => Chunk(data)
        }
        Chunked(playChunks, Some(ct.toString()))
      case e => throw new NotImplementedError(s"Unexpected response entity type: ${e.getClass.getName}")
    }
    Result(pekkoToPlayResponseHeaders(pekkoResp), playEntity)
  }

  def pekkoToPlayHeaders(headers: immutable.Seq[HttpHeader]): Headers = {
    Headers(headers.map(h => (h.name(), h.value())): _*)
  }

  def pekkoToPlayResponseHeaders(resp: HttpResponse): ResponseHeader = {
    ResponseHeader(
      status = play.api.http.Status.OK,
      headers = pekkoToPlayHeaders(resp.headers).toSimpleMap,
    )
  }
}
