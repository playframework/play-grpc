/*
 * Copyright (C) 2018-2019 Lightbend Inc. <https://www.lightbend.com>
 */
package play.grpc

import akka.NotUsed
import akka.actor.ActorSystem
import akka.grpc.scaladsl.GrpcMarshalling
import akka.grpc.Codec
import akka.grpc.Grpc
import akka.grpc.ProtobufSerializer
import akka.http.scaladsl.marshalling.Marshaller
import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model.HttpEntity.Chunk
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.stream.ActorMaterializer
import akka.stream.Materializer
import akka.util.ByteString
import controllers.GreeterServiceImpl
import example.myapp.helloworld.grpc.GreeterService
import example.myapp.helloworld.grpc.HelloReply
import example.myapp.helloworld.grpc.HelloRequest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.BeforeAndAfterAll
import play.api.libs.typedmap.TypedMap
import play.api.mvc.Headers
import play.api.mvc.RequestHeader
import play.api.mvc.akkahttp.AkkaHttpHandler
import play.api.mvc.request.RemoteConnection
import play.api.mvc.request.RequestFactory
import play.api.mvc.request.RequestTarget

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PlayJavaRouterSpec extends AnyWordSpec with Matchers with BeforeAndAfterAll with ScalaFutures {
  implicit val sys      = ActorSystem()
  implicit val mat      = ActorMaterializer()
  implicit val ec       = sys.dispatcher
  implicit val patience = PatienceConfig(timeout = 3.seconds, interval = 15.milliseconds)

  // serializers so we can test the requests
  implicit val HelloRequestSerializer = example.myapp.helloworld.grpc.GreeterService.Serializers.HelloRequestSerializer
  implicit val HelloReplySerializer   = example.myapp.helloworld.grpc.GreeterService.Serializers.HelloReplySerializer

  implicit def unmarshaller[T](
      implicit serializer: ProtobufSerializer[T],
      mat: Materializer,
  ): FromRequestUnmarshaller[T] =
    Unmarshaller((ec: ExecutionContext) ⇒ (req: HttpRequest) ⇒ GrpcMarshalling.unmarshal(req)(serializer, mat))

  implicit def toSourceUnmarshaller[T](
      implicit serializer: ProtobufSerializer[T],
      mat: Materializer,
  ): FromRequestUnmarshaller[Source[T, NotUsed]] =
    Unmarshaller((ec: ExecutionContext) ⇒ (req: HttpRequest) ⇒ GrpcMarshalling.unmarshalStream(req)(serializer, mat))

  implicit def marshaller[T](
      implicit serializer: ProtobufSerializer[T],
      mat: Materializer,
      codec: Codec,
      system: ActorSystem,
  ): ToResponseMarshaller[T] =
    Marshaller.opaque((response: T) ⇒ GrpcMarshalling.marshal(response)(serializer, mat, codec, system))

  implicit def fromSourceMarshaller[T](
      implicit serializer: ProtobufSerializer[T],
      mat: Materializer,
      codec: Codec,
      system: ActorSystem,
  ): ToResponseMarshaller[Source[T, NotUsed]] =
    Marshaller.opaque(
      (response: Source[T, NotUsed]) ⇒ GrpcMarshalling.marshalStream(response)(serializer, mat, codec, system),
    )

  val router = new GreeterServiceImpl(mat, sys)

  "The generated Play (Java) Router" should {

    "don't accept requests for other paths" in {
      router.routes.lift(playRequestFor(Uri("http://localhost/foo"))).isEmpty shouldBe true
    }

    "accept requests using the service name as prefix" in {
      val uri = Uri(s"http://localhost/${GreeterService.name}/SayHello")
      router.routes.lift(playRequestFor(uri)).isDefined shouldBe true

      val name = "John"

      val handler  = router.routes(playRequestFor(uri)).asInstanceOf[AkkaHttpHandler]
      val request  = akkaHttpRequestFor(uri, HelloRequest.newBuilder().setName(name).build())(HelloRequestSerializer)
      val response = handler(request).futureValue
      response.status shouldBe StatusCodes.OK

      val reply = akkaHttpResponse[HelloReply](response).futureValue
      reply.getMessage shouldBe s"Hello, $name!"
    }

    "allow / as identity prefix" in {
      val result = router.withPrefix("/")
      result shouldBe theSameInstanceAs(router)
    }

    "not allow specifying another prefix" in {
      intercept[UnsupportedOperationException] {
        router.withPrefix("/some")
      }
    }

    def akkaHttpRequestFor[T](uri: Uri, msg: T)(implicit serializer: ProtobufSerializer[T]) = {
      HttpRequest(
        uri = uri,
        entity = HttpEntity.Chunked(
          Grpc.contentType,
          Source.single(msg).map(serializer.serialize).via(Grpc.grpcFramingEncoder).map(Chunk(_)),
        ),
      )
    }
    def akkaHttpResponse[T](response: HttpResponse)(implicit deserializer: ProtobufSerializer[T]) =
      response.entity.dataBytes
        .via(Grpc.grpcFramingDecoder)
        .runWith(Sink.reduce[ByteString](_ ++ _))
        .map(deserializer.deserialize)

    def playRequestFor(uri: Uri): RequestHeader =
      RequestFactory.plain.createRequest(
        RemoteConnection(uri.authority.host.address, secure = false, clientCertificateChain = None),
        "GET",
        RequestTarget(uri.toString, uri.path.toString, queryString = Map.empty),
        version = "42",
        Headers(),
        attrs = TypedMap.empty,
        body = (),
      )
  }

  override def afterAll(): Unit = {
    super.afterAll()
    sys.terminate()
    ()
  }
}
