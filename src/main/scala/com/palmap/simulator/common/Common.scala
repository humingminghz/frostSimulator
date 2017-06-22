package com.palmap.simulator.common

/**
  * Created by Administrator on 2017/6/20.
  */
object Common {
  val SCENEID = "sceneId"
  val DATE = "date"
  val HOURS = "hours"
  val VISITOR_PER_HOUR= "visitorPerHour"
  val REPEAT_PER_HOUR = "repeat"
  val BEGIN_ID = "idFrom"
  val KAFKA_HOST = "kafka_host"
  val KAFKA_TOPIC = "topic_name"
  val MAC_HEAD = "macHead"
  val LOG_LEVEL = "akka.loglevel"

  val DAY_FORMAT = "yyyy-MM-dd"
  val DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"

  val MINUTE = 60000L
  val HOUR: Long = 60L * MINUTE
  val REPEAT_TIMES = 5
}
