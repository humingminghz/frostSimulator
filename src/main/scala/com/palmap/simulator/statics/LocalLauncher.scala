package com.palmap.simulator.statics

import akka.actor.ActorSystem
import com.palmap.simulator.actor.RssiInfoActor
import com.palmap.simulator.common.{Common, DateUtil}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.slf4j.LazyLogging

import scala.collection.mutable
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout


/**
  * Created by Mingming,hu on 2017/6/20.
  */
object LocalLauncher extends LazyLogging{

  /**
    * launch from...
    * @param args
    */
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val sceneId = config.getInt("sceneId")
    val date = DateUtil.getDateByString(config.getString(Common.DATE))
    val hours = config.getString(Common.HOURS)
    val visitorPerHour = config.getString(Common.VISITOR_PER_HOUR)
    val repeatPerHour = config.getString(Common.REPEAT_PER_HOUR)
    val macHead = config.getString(Common.MAC_HEAD)

    // 初始化小时-人数map
    logger.info(s"Init hour visitor Map ${System.currentTimeMillis()}")
    val hourVisitorMap = initHourMap(hours, visitorPerHour)

    //初始化小时-重复人数
    logger.info("Init hour repeat Map")
    val hourRepeatMap = initHourMap(hours, repeatPerHour)

    val hourSet = hourRepeatMap.keySet
    val system = ActorSystem("RSSI")

    implicit val timeout = Timeout(10 minute)
    implicit val ec =  scala.concurrent.ExecutionContext.Implicits.global

    hourSet.foreach(hour => {
      val actor = system.actorOf(RssiInfoActor.props, hour.toString)
      val visitorNum = hourVisitorMap.getOrElse(hour, 0)
      val repeatNum = hourRepeatMap.getOrElse(hour, 0)

      val future = actor ? RssiInfoActor.sendRssiInfo(sceneId, date, hour,visitorNum , repeatNum, macHead)
      future onSuccess{
        case s:String => logger.info(s)
        case _ => logger.info("unknown message")
      }

      future onFailure{
        case e:Exception => logger.error(e.getMessage)
      }
    })

    system.scheduler.scheduleOnce(3 minute) { // 3分钟没跑完会直接shutdown
      system.shutdown()
    }
  }

  /**
    * 初始化hour-visitor hour-repeatVisitor Map
    * @param hour application.conf 中hours字段
    * @param numberString application.conf 中visitorPerHour或repeat字段
    * @return Map[Hour, Number]
    */
  def initHourMap(hour : String, numberString: String) : mutable.Map[Int, Int] ={
    val resultMap = mutable.Map[Int, Int]()

    val hourRange = hour.split("-").map(x => x.toInt)
    val visitors = numberString.split("-").map(x => x.toInt)

    if(hourRange.length != 2){
      logger.error(s"Hour Range Incorrect, size is ${hourRange.length}")
    }

    val hourList = mutable.ListBuffer[Int]()
    for(i <- hourRange(0) to hourRange(1)){
      hourList.append(i)
    }

    if(hourList.length != visitors.length){
      logger.error("Size not match, cannot init Hour Visitor Map")
      sys.exit(-1)
    }

    for(i <- hourList.indices){
      resultMap.put(hourList(i), visitors(i))
    }

    resultMap
  }

}
