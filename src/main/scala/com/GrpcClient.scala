package com

import Log.{LogProcessorGrpc, LogReply, LogRequest}
import com.Utilities.CreateLogger
import com.typesafe.config.ConfigFactory
import io.grpc.ManagedChannelBuilder
import io.grpc.netty.NettyChannelBuilder
import scalaj.http.Http
class GrpcClient {

}


object GrpcClient  {
  val config = ConfigFactory.load()
  val logger = CreateLogger(classOf[GrpcClient])

  def main(args: Array[String]) = {
    val result = grpc()
    logger.info(s"Result ${result}")
  }
  def grpc()={
    val channelBuilder = ManagedChannelBuilder.forAddress("localhost", config.getInt("log.grpc_port"))
    channelBuilder.usePlaintext()
    val channel = channelBuilder.build()
    val request=LogRequest(config.getString("log.search_string_time"),config.getString("log.search_string_interval"))
    val blockingStub = LogProcessorGrpc.blockingStub(channel)
    val reply: LogReply = blockingStub.findLog(request)
    if(reply.message.equals(""))
      logger.info("No log messages found")
    else
    logger.info(s"Hash of log message = ${reply.message}")
  }
}
