package com

import com.Utilities.{CreateLogger, HelperUtils}
import com.amazonaws.AmazonServiceException
import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.typesafe.config.ConfigFactory

import java.io.{FileNotFoundException, IOException}
import scala.io.Source



class AccessS3 {

}

object AccessS3 {
  val config = ConfigFactory.load()
  val logger = CreateLogger(classOf[AccessS3])

  // assuming the interval given doesn't exceed the day boundary
  def access(dateString:String,lambdaLogger:LambdaLogger): Array[String] = {
    val bucket_name = config.getString("log.s3_bucket_name")
    val key_name = config.getString("log.s3_path")
    val region = config.getString("log.s3_region") match {
      case "US-EAST-1" => Regions.US_EAST_1
    }

    lambdaLogger.log(s"Downloading  $key_name from S3 bucket $bucket_name..")
    val s3Client = AmazonS3ClientBuilder.standard.withRegion(region).build
    try {
      val dateComponent = HelperUtils.getDateComponentString(dateString)
      val fileName = s"${key_name}/LogFileGenerator.${dateComponent}.log"
      val logObject = s3Client.getObject(bucket_name, fileName)
      val logObjectContent = logObject.getObjectContent
      val myData = Source.fromInputStream(logObjectContent)

      lambdaLogger.log(s"Starting fetch from S3 ${fileName}" )
      val runid = myData.getLines().toArray
      lambdaLogger.log(s"Fetch complete len of data = ${runid.length}")
      logObjectContent.close()
      return runid
    } catch {
      case e: AmazonServiceException =>
        logger.error(e.getErrorMessage)
        lambdaLogger.log(e.getMessage)
        System.exit(1)
      case e: FileNotFoundException =>
        logger.error(e.getMessage)
        lambdaLogger.log(e.getMessage)
        System.exit(1)
      case e: IOException =>
        logger.error(e.getMessage)
        lambdaLogger.log(e.getMessage)
        System.exit(1)
    }
    logger.info("Failed to get object, hence returning empty array")
    lambdaLogger.log("failed")
    Array[String]()
  }
}