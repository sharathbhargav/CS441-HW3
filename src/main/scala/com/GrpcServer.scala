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

/**
 * This class creates the GRPC server and allows clients to invoke methods remotely.
 * It inturn makes a http request to lambda function hosted on AWS to get the hash of a set of matching log messages.
 * @param executionContext
 */
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
      if(response.code==200) {
        // Parse response from API to protobuf Response object
        try {
          val responseMessage = LogReply.parseFrom(response.body)
          Future.successful(responseMessage)
        }
        catch {
          case e: Exception =>
            Future.failed(e)
        }
      }
      else{
        Future.successful(LogReply(""))
      }
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
