package com.Utilities

import com.log_process1.{config, dateFormatter}

import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}

class HelperUtils {

}

object HelperUtils {
  val dateFormatter = new SimpleDateFormat(config.getString("log.date_time_format"))
  val tz: TimeZone = TimeZone.getTimeZone(config.getString("log.time_zone"))
  val intervalFormatter = new SimpleDateFormat("HH:mm:ss.S")
  dateFormatter.setTimeZone(tz)
  intervalFormatter.setTimeZone(tz)

  /**
   * This function is used to obtain just the date component from a date time string.
   * @param dateString
   * @return string representing date
   */
  def getDateComponentString(dateString:String):String={
    val full_date=dateFormatter.parse(dateString)
    val dateFormatter2 = new SimpleDateFormat("yyyy-MM-dd")
    return dateFormatter2.format(full_date)
  }

  /**
   * This function is used to obtain only the time component of a date time string
   * @param dateString
   * @return Date object representing only the time component
   */
  def getTimeComponent(dateString:String):Date = {
    val full_date=dateFormatter.parse(dateString)
    val dateFormatter2 = new SimpleDateFormat("HH:mm:ss.S")

    return dateFormatter2.parse(dateFormatter2.format(full_date))
  }

  /**
   * Function used to convert a time string into date object.
   * @param dateString
   * @return Date object of the time input
   */
  def getIntervalTime(dateString:String):Date={
    val interval = intervalFormatter.parse(dateString)
    return interval
  }
}
