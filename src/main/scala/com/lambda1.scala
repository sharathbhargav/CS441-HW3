package com

import Log.{LogReply, LogRequest}
import com.Utilities.HelperUtils
import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import org.json4s.jackson.JsonMethods._

import java.util.Base64
import scala.collection.convert.ImplicitConversions.`map AsScala`
import scala.jdk.CollectionConverters.MapHasAsJava


//import hello.{HelloReply, HelloRequest}


class lambda1 extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent] {

  /**
   * Handler for the AWS Lambda function.
   */
  override def handleRequest(input: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {

    val logger = context.getLogger
    logger.log(s"Request ${input.toString}")
    val headers = input.getHeaders
    val key_name =  if( headers.containsKey("content-type")) "content-type"  else  "Content-Type"
    val dat = headers(key_name) match {
      case "application/grpc+proto" => {
        logger.log("Input header contains application/grpc+proto")
        val message = if (input.getIsBase64Encoded) Base64.getDecoder.decode(input.getBody.getBytes) else input.getBody.getBytes
        LogRequest.parseFrom(message)
      }
      case "application/json" => {
        implicit val formats = org.json4s.DefaultJsonFormats
        val jsonData = parse(input.getBody).values.asInstanceOf[Map[String, Any]]
        logger.log(s"Json converted = ${jsonData("time")}, interval = ${jsonData("interval")}")
        new LogRequest(jsonData("time").toString, jsonData("interval").toString)
      }
      case _ => {
        logger.log("Unknown content-type")
        new LogRequest("18:28:03.302", "00:00:00.500")
      }
    }
    val dateComponent = HelperUtils.getDateComponentString(dat.time)
    logger.log(s"Parsed date component = ${dateComponent}")
    val data = AccessS3.access(dat.time,logger)
    logger.log(s"data = ${dat.time}")
    val hash = log_process1.start(dat.time, dat.interval, data, logger)

    logger.log(s"Hash output =${hash}")
    val output = Base64.getEncoder.encodeToString(LogReply(hash).toByteArray)
    logger.log(s"Output: $output")
    logger.log("End of output")
    if (headers("content-type").equals("application/grpc+proto")) {
      val apiResponse = new APIGatewayProxyResponseEvent()

      if (hash.equals("")) {
        logger.log("No hash sp 404")
        apiResponse.withStatusCode(404)
      }
      else {
        logger.log("Sending api response")
        apiResponse.withStatusCode(200)
        apiResponse.withHeaders(Map("Content-Type" -> "application/grpc+proto").asJava)
          .withIsBase64Encoded(true)
          .withBody(output)
      }
      return apiResponse
    }
    else {
      val apiResponse = new APIGatewayProxyResponseEvent()

      if (hash.equals("")) {
        apiResponse.withStatusCode(404)
      }
      else {
        apiResponse.withStatusCode(200)
          .withHeaders(Map("Content-Type" -> "application/json").asJava)
          .withIsBase64Encoded(false)
          .withBody(
            s"""{
          "hash":${hash}
        }""")
      }
      return apiResponse
    }
  }
}
