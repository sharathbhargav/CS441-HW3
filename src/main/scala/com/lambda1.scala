package com

import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}

import java.util.Base64
import scala.collection.JavaConverters._
import scala.collection.convert.ImplicitConversions.`map AsScala`
//import hello.{HelloReply, HelloRequest}


class lambda1 extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent] {

  /**
   * Handler for the AWS Lambda function.
   */
  override def handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {

    val logger = context.getLogger
    logger.log(s"Request ${input.toString}")
    val headers = input.getHeaders
    if (headers("Content-Type").equals("application/grpc+proto")){
      logger.log("Input header contains application/grpc+proto")
    }
    logger.log("Printing header>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
    headers.foreach {keyVal => logger.log(keyVal._1 + "=" + keyVal._2)}
    // Decode base-64 encoded binary data from the request body
    val message = if (input.getIsBase64Encoded) Base64.getDecoder.decode(input.getBody.getBytes) else input.getBody.getBytes
    logger.log(s"message: (${message.mkString(", ")})")
    val dat = hello.LogRequest.parseFrom(message)
//    logger.log(s"Input data= $dat")


    val data = AccessS3.access()
    val hash = log_process1.start(dat.time, dat.interval, data)
    val output = Base64.getEncoder.encodeToString(hello.LogReply(hash).toByteArray)
    logger.log(s"Output: $output")
    // Send the response
    logger.log("End of output")
    new APIGatewayProxyResponseEvent()
      .withStatusCode(200)
//      .withHeaders(Map("Content-Type" -> "application/grpc+proto","grpc-status"->0).asJava)
      .withHeaders(Map("Content-Type" -> "application/grpc+proto").asJava)

      .withIsBase64Encoded(true)
      .withBody(output)
  }
}
