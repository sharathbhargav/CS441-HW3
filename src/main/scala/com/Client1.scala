package com

import com.Utilities.CreateLogger
import com.typesafe.config.ConfigFactory
import hello.{LogReply, LogRequest}
import scalaj.http.Http
class Client1 {

}


object Client1  {
  val config = ConfigFactory.load()
  val logger = CreateLogger(classOf[Client1])

  def main(args: Array[String]) = {
    val result = grpc()
    logger.info(s"Result ${result}")
  }
  def grpc()={
    val request = Http(config.getString("log.lambda_link"))
      .headers(Map(
        "Content-Type" -> "application/grpc+proto",
        "Accept" -> "application/grpc+proto"
      )).timeout(connTimeoutMs = config.getInt("log.grpc_conn_timeout"),
        readTimeoutMs = config.getInt("log.grpc_read_timeout"))
      .postData(LogRequest("12:48:20.048","00:00:00.900").toByteArray)

    val response = request.asBytes
    logger.info(s"Got response: $response")

    // Parse response from API to protobuf Response object
    val responseMessage = LogReply.parseFrom(response.body)

    responseMessage.message
  }
}
