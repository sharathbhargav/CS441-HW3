package com

import com.Utilities.CreateLogger
import com.amazonaws.AmazonServiceException
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.typesafe.config.ConfigFactory

import java.io.{FileNotFoundException, IOException}
import scala.io.Source


class AccessS3 {

}

object AccessS3 {
  val config = ConfigFactory.load()
  val logger = CreateLogger(classOf[AccessS3])

  def access(): Array[String] = {
    val bucket_name = config.getString("log.s3_bucket_name")
    val key_name = config.getString("log.s3_path")
    val region = config.getString("log.s3_region") match {
      case "US-EAST-1" => Regions.US_EAST_1
    }
    logger.info("Downloading %s from S3 bucket %s...\n", key_name, bucket_name)

    val s3Client = AmazonS3ClientBuilder.standard.withRegion(region).build
    try {
      val logObject = s3Client.getObject(bucket_name, key_name)
      val logObjectContent = logObject.getObjectContent
      val myData = Source.fromInputStream(logObjectContent)
      logger.info("Starting fetch from S3")
      val runid = myData.getLines().toArray
      logger.info("Fetch complete")
      logObjectContent.close()
      return runid
    } catch {
      case e: AmazonServiceException =>
        logger.error(e.getErrorMessage)
        System.exit(1)
      case e: FileNotFoundException =>
        logger.error(e.getMessage)
        System.exit(1)
      case e: IOException =>
        logger.error(e.getMessage)
        System.exit(1)
    }
    logger.info("Failed to get object, hence returning empty array")
    Array[String]()
  }
}