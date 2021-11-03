package com

import Log.LogProcessorGrpc.LogProcessor
import Log.{LogReply, LogRequest}
import com.Utilities.CreateLogger
import com.typesafe.config.ConfigFactory
import io.grpc.netty.NettyServerBuilder
import io.grpc.{Server, ServerBuilder}
import scalaj.http.Http

import scala.concurrent.{ExecutionContext, Future}

object GrpcServer  {
  def main(args: Array[String]):Unit = {
    val server = new GrpcServer(ExecutionContext.global)
    server.start()
    server.blockUntilShutdown()
  }
}

class GrpcServer(executionContext: ExecutionContext) {
  val config = ConfigFactory.load()
  val logger = CreateLogger(classOf[GrpcClient])
  class LogProcessorClass extends LogProcessor{
    override def findLog(request: LogRequest): Future[LogReply] ={
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
      Future.successful(responseMessage)
    }
  }
  var server: Server = null

  def start() = {
    val serverBuilder = NettyServerBuilder.forPort(config.getInt("log.grpc_port"))
    serverBuilder.addService(LogProcessor.bindService(new LogProcessorClass, executionContext))
    server = serverBuilder.build().start()
    sys.addShutdownHook {
      if (server != null)
        server.shutdown()
    }
  }

  def blockUntilShutdown() = {
    if (server != null)
      server.awaitTermination()
  }

}
