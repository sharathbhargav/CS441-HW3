package com

import Log.{LogProcessorGrpc, LogReply, LogRequest}
import com.Utilities.CreateLogger
import com.typesafe.config.ConfigFactory
import io.grpc.ManagedChannelBuilder
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
    val request=LogRequest("2021-11-01 10:22:22.540","00:00:00.900")
    val blockingStub = LogProcessorGrpc.blockingStub(channel)
    val reply: LogReply = blockingStub.findLog(request)
    println(reply.message)
  }
}
