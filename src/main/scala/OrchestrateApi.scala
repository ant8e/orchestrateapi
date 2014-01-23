package net.antoinecomte

import akka.actor.ActorSystem
import akka.io.IO
import scala.concurrent.{Await, Future}
import spray.can.Http
import spray.http._
import spray.client.pipelining._
import spray.httpx.encoding.Gzip
import spray.httpx.unmarshalling.{BasicUnmarshallers, Unmarshaller}
import spray.json.{RootJsonWriter, RootJsonReader}
import akka.event.Logging
import spray.httpx.{PipelineException, UnsuccessfulResponseException}

/**
 * Import net.antoine.comte.orchestreapi
 */
package object orchestrateapi extends spray.httpx.SprayJsonSupport {
  implicit val as = ActorSystem("OrchestraApi")
  implicit val ds = as.dispatcher

  case class Settings(url: String, apiKey: String)


  type Ref = String

  class ClientException(message: String, cause: Throwable = null) extends RuntimeException(message, cause)

  trait Client {
    def settings: Settings

    private val pipeline =
      addCredentials(BasicHttpCredentials(settings.apiKey, null)) ~>
        logRequest(as.log, Logging.DebugLevel) ~>
        encode(Gzip) ~>
        sendReceive



    def getKey(collection: String, key: String, ref: Option[Ref] = None) = new keyGetter(collection, key, ref)

    class keyGetter(collection: String, key: String, ref: Option[Ref]) {

      def as[T: RootJsonReader]: Future[Option[T]] = doAs

      def asJson: Future[Option[String]] = doAs(BasicUnmarshallers.StringUnmarshaller)

      private def doAs[T: Unmarshaller]: Future[Option[T]] = {
        val pipe = pipeline ~> unmarshal[T]
        val future: Future[T] = pipe(Get(collectionKeyUrl(collection, key, ref)))
        future.map(Some(_)).recover {
          case e: UnsuccessfulResponseException => None
          case e: PipelineException => throw new ClientException(e.getMessage, e.getCause)
        }
      }

    }

    def putKey(collection: String, key: String, value: String): Future[Int] = {
      val f: Future[HttpResponse] = pipeline(Put(collectionKeyUrl(collection, key), HttpEntity(ContentTypes.`application/json`, value)))
      f.map(_.status.intValue)
    }

    def putKey[T: RootJsonWriter](collection: String, key: String, value: T): Future[Int] = (pipeline(Put(collectionKeyUrl(collection, key), value)))
      .map(_.status.intValue)


    private def collectionKeyUrl(collection: String, key: String, ref: Option[Ref] = None): String = s"${settings.url}/$collection/$key${ref.map(r => s"/refs/${r}").getOrElse("")}"
  }


  def Client(s: Settings): Client = new Client {
    def settings: Settings = s
  }

  def Client(apiKey: String, url: String = "https://api.orchestrate.io/v0"): Client = new Client {
    def settings: Settings = Settings(url, apiKey)
  }


  def shutdown(): Unit = {
    import akka.pattern.ask
    import scala.concurrent.duration._
    import spray.util._
    IO(Http).ask(Http.CloseAll)(1.second).await
    as.shutdown()
  }

}
