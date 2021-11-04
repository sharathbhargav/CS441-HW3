package com.akka

import Log.{LogReply, LogRequest}
import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.server.directives.{PathDirectives, RouteDirectives}
import com.Utilities.CreateLogger
import com.typesafe.config.ConfigFactory
import scalaj.http.Http
import spray.json.DefaultJsonProtocol

final case class LogResponseAkka(hash: String)

case class LogRequestAkka(time: String, interval: String)

trait LogAkkaObj extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val logFormat = jsonFormat1(LogResponseAkka)
  implicit val logRequestFormat = jsonFormat2(LogRequestAkka)
}


/**
 * This class defines the routes that are supported by the Akka web server. For each route defined, a set of actions can
 * be defined. In this class the POST and GET methods make a http call to lambda function hosted on AWS to get the hash
 * of a set of matching log messages with a time interval.
 * @param system
 */
class RouteConfig(implicit val system: ActorSystem) extends Directives with LogAkkaObj {

  val config = ConfigFactory.load()
  val logger = CreateLogger(classOf[RouteConfig])
  val getRoute: Route = PathDirectives.pathPrefix("log") {
    path("get") {
      parameters("time", "interval") { (time, interval) =>
        Directives.get {
          val request = LogRequest(time = time, interval = interval)
          val responseMessage = makeHttpRequest(request)
          if (responseMessage.message.equals(""))
            RouteDirectives.complete(StatusCodes.NotFound)
          else
            RouteDirectives.complete(LogResponseAkka(responseMessage.message))
        }
      }
    }
  }

  val postRoute: Route = PathDirectives.pathPrefix("log") {
    path("post") {
      entity(as[LogRequestAkka]) {
        l => {
          Directives.post {
            val responseMessage = makeHttpRequest(new LogRequest(l.time, l.interval))
            if (responseMessage.message.equals(""))
              RouteDirectives.complete(StatusCodes.NotFound)
            else
              RouteDirectives.complete(LogResponseAkka(responseMessage.message))
          }
        }
      }
    }
  }

  def makeHttpRequest(request: LogRequest): LogReply = {
    val httpRequest = Http(config.getString("log.lambda_link"))
      .headers(Map(
        "Content-Type" -> "application/grpc+proto",
        "Accept" -> "application/grpc+proto"
      )).timeout(connTimeoutMs = config.getInt("log.grpc_conn_timeout"),
      readTimeoutMs = config.getInt("log.grpc_read_timeout"))
      .postData(request.toByteArray)

    val response = httpRequest.asBytes
    logger.info(s"Got response: $response")
    logger.info(s"Got response: ${response.body.mkString}")

    // Parse response from API to protobuf Response object
    val responseMessage = LogReply.parseFrom(response.body)
    responseMessage
  }
}
