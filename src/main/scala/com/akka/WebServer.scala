package com.akka

class WebServer {

}
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.PathDirectives.pathPrefix
import akka.stream.ActorMaterializer


import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object WebServer {
  implicit val system: ActorSystem = ActorSystem("web-app")
  private implicit val dispatcher: ExecutionContextExecutor = system.dispatcher
  private implicit val materialize: ActorMaterializer = ActorMaterializer()

  def main(args: Array[String]):Unit = {

    val routeConfig = new RouteConfig()
    val routes = {
      pathPrefix("api") {
        concat(
          routeConfig.getRoute,routeConfig.postRoute

        )
      }
    }
    val serverFuture = Http().bindAndHandle(routes, "localhost", 8080)

    println("Server started ...")
    StdIn.readLine()
    serverFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}