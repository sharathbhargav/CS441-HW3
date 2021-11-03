package com

import com.Utilities.CreateLogger
import com.Utilities.HelperUtils.{ getIntervalTime, getTimeComponent}
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.Logger

import java.math.BigInteger
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}
import java.util.regex.Pattern

class log_process1 {

}

object log_process1 {
  val config: Config = ConfigFactory.load()
  val logger: Logger = CreateLogger(classOf[GrpcClient])
  val pattern: Pattern = Pattern.compile(config.getString("log.log_pattern"))
  val GLOBAL_PATTERN: Pattern = Pattern.compile(config.getString("log.detect_pattern"))
  val dateFormatter = new SimpleDateFormat(config.getString("log.date_time_format"))
  val tz: TimeZone = TimeZone.getTimeZone(config.getString("log.time_zone"))
  dateFormatter.setTimeZone(tz)

  val timeFormatter = new SimpleDateFormat("HH:mm:ss.S")
//  val lines = Source.fromFile("/home/sharath/fall_2021/441/a3/scala_lambd/logs/LogFileGenerator.1.log").getLines.toArray

  def main(args: Array[String]):Unit={
  }

  def start(time1: String="18:28:03.302", time_interval: String="00:00:00.500",lines : Array[String], lambdaLogger:LambdaLogger): String = {
//    val interval = time_interval
    val time_required_formatted = getTimeComponent(time1)
    val time_interval_formatted = getIntervalTime(time_interval)
    val start_time_object = time_required_formatted.getTime - time_interval_formatted.getTime
    val end_time_object = time_required_formatted.getTime + time_interval_formatted.getTime
    lambdaLogger.log(s"time required formatted = ${time_required_formatted.toString}")
    lambdaLogger.log(s"start time = $start_time_object end time = $end_time_object,  lines1 = ${lines(0)}, len = ${lines.length}")
    if (checkRange(start_time_object, end_time_object, lines(0), lines(lines.length - 1),lambdaLogger)) {
      val start_line = IterativeBinarySearch(lines, start_time_object, 0, lines.length - 1)
      val end_line = IterativeBinarySearch(lines, end_time_object, start_line, lines.length - 1)
      val newList = lines.slice(start_line, end_line)

      val selected = newList.withFilter(GLOBAL_PATTERN.matcher(_).find()==true).map( str => {
        val matcher = GLOBAL_PATTERN.matcher(str)
        matcher.find()
        matcher.group(0)
      })
      lambdaLogger.log(s"Seected msgs = ${selected.mkString("\n")}")
      val hash = md5(selected.mkString("\n"))
      return hash
    }
    else{
      lambdaLogger.log("Check range failed")
    }
    return ""
  }

  def md5(s: String) = {
    val digest = MessageDigest.getInstance("MD5").digest(s.getBytes)
    val bigInt = new BigInteger(1, digest)
    val hashedString = bigInt.toString(16)
    hashedString
  }

  def checkRange(t1: Long, t2: Long, r1: String, r2: String,lambdaLogger:LambdaLogger): Boolean = {
    val m1 = pattern.matcher(r1)
    val m2 = pattern.matcher(r2)

    if (m1.find() && m2.find()) {
      val range1 = getIntervalTime(m1.group(1).trim)
      val range2 = getIntervalTime(m2.group(1).trim)
      val tt1 = new Date(t1)
      val tt2 = new Date(t2)
      lambdaLogger.log(s"first time = ${tt1.getTime}  second time = ${tt2.getTime}")
      tt1.after(range1) && tt2.before(range2)
    }
    else
      false
  }

  def IterativeBinarySearch(arr: Array[String],
                            el: Long, l: Int, h: Int): Int = {
    var low = l
    var high = h
    var diff: Long = el - getDateObj(arr(l)).getTime
    var index = 0
    while (low <= high) {
      var middle = low + (high - low) / 2
      if (getDateObj(arr(middle)).getTime == el)
        return middle
      else if (getDateObj(arr(middle)).getTime > el) {
        val d1 = getDateObj(arr(middle)).getTime - el
        if (d1 < diff) {
          diff = d1
          index = middle
        }
        high = middle - 1
      } else {
        val d1 = el - getDateObj(arr(middle)).getTime
        if (d1 < diff) {
          diff = d1
          index = middle
        }
        low = middle + 1
      }
    }
    return index
  }

  def getDateObj(line: String): Date = {
    val matcher = pattern.matcher(line)
    if (matcher.find()) {
      val tt1 = matcher.group(1).trim
      val dt1 = getIntervalTime(tt1)
      return dt1
    }
    return new Date(0)
  }

}