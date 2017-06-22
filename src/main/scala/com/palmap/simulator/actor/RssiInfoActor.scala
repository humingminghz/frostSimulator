package com.palmap.simulator.actor

import akka.actor.{Actor, Props}
import com.palmap.rssi.message.FrostEvent.{IdType, RssiInfo, RssiItem, StubType}
import com.palmap.simulator.common.{Common, DateUtil, Producer}
import com.typesafe.scalalogging.slf4j.LazyLogging

import scala.collection.mutable

/**
  * Created by Mingming.hu on 2017/6/20.
  */
class RssiInfoActor extends Actor with LazyLogging{
  import com.palmap.simulator.actor.RssiInfoActor.sendRssiInfo

  /**
    * 按每小时向kafka发送RssiInfo的消息
    * @param date 日期
    * @param hour 小时
    * @param visitorNum 总人数
    * @param repeatNum 重复的人数
    */
  def sendRssiInfoByTime(sceneId : Int, date : Long, hour : Int, visitorNum : Int, repeatNum : Int, macHead: String) : Unit ={
    logger.info(s"start with date: ${DateUtil.getDateByLong(date)} hour: $hour")
    val repeatMacSet = mutable.Set[String]() // 用做重复的数据
    var repeatTo = 0
    var repeatToStart = false
    var repeatTimes = 0

    val hourTime = date + hour.toLong * Common.HOUR

    if(visitorNum < 60){ // 平均每分钟不足一个mac的情况下 在同一分钟内一次性发送所有mac
      for(_ <- 0 until visitorNum){
        val mac = generateMac(macHead)
        sendRssiInfo(sceneId, mac, hourTime)
      }

    }else{
      val visitorPerMin = visitorNum / 60  // 平均每分钟发多少mac
      val pplLeft = visitorNum % 60  // 平均后多余的mac数


      for(i <- 0 until 60){
        val time = hourTime + Common.MINUTE * i.toLong

        var numberOfMacToSend = 0
        //第一次发送等于每分钟平均数 + 平均后剩余值
        if(i == 0) {
          numberOfMacToSend = visitorPerMin + pplLeft
        }else{
          numberOfMacToSend = visitorPerMin
        }

        for(_ <- 0 until numberOfMacToSend){
          val mac = generateMac(macHead)
          sendRssiInfo(sceneId, mac, time)
          // 准备可以重复发送的mac
          if(repeatTo <= repeatNum ){
            repeatTo += 1
            repeatMacSet.add(mac)
          }else{
            repeatToStart = true
          }

          // 可以重复的mac准备就绪后就连续发送5分钟
          if(repeatToStart && repeatMacSet.nonEmpty){
            repeatTimes += 1
            if(repeatTimes <= Common.REPEAT_TIMES){
              repeatMacSet.foreach(mac => {
                sendRssiInfo(sceneId, mac, time)
              })
            }
          }
        }
      }
    }
  }

  /**
    * 根据预先设置好的package.Id顺序生成mac地址
    * @return mac  "FF:FF:FF:FF:FF:FF"
    */
  def generateMac(macHead : String) : String ={
    var idHex = id.toHexString
    id += 1
    while(idHex.length < 6){
      idHex = "0" + idHex
    }

    val finalMacString = StringBuilder.newBuilder
    finalMacString.append(macHead) // 选择其中一个mac头 前6个16进制数代表品牌
    for(i <- 0 to 2){
      finalMacString.append(":")
      finalMacString.append(idHex.substring(2*i, 2*i +2))
    }

    finalMacString.toString()
  }

  /**
    * 生成RssiInfo proto object
    * @param sceneId 场景id
    * @param mac mac地址
    * @param time 时间戳
    */
  private def sendRssiInfo(sceneId : Int, mac : String, time : Long) : Unit ={
    // 生成protobuf相关对象
    val rssiItem = RssiItem.newBuilder()
    rssiItem.setIdData(mac)
    rssiItem.setIdType(IdType.MAC)
    rssiItem.setRssi(3)
    rssiItem.setConnected(true)

    val rssiInfo = RssiInfo.newBuilder()
    rssiInfo.addItems(rssiItem)
    rssiInfo.setIdData("20:76:93:2a:ee:0c")
    rssiInfo.setIdType(IdType.MAC)
    rssiInfo.setStubType(StubType.AP)
    rssiInfo.setTimestamp(time)
    rssiInfo.setSceneId(sceneId.toLong)

    Producer.send(DateUtil.getNowDateString, rssiInfo.build().toByteArray) // 向kafka发送消息
  }

  /**
    * actor receive method
    * @return
    */
  override def receive: Receive = {
    case sendRssiInfo(sceneId, date, hour, visitorNum, repeatNum, macHead) =>
      sendRssiInfoByTime(sceneId, date, hour, visitorNum, repeatNum, macHead)
      sender() ! s"success with date: $date hour: $hour visitorNum: $visitorNum repeatNum: $repeatNum macHead: $macHead ${System.currentTimeMillis()}"
      context stop  self

    case _ =>
  }
}

/**
  * 伴生对象 actor调用时用
  */
object RssiInfoActor {
  val props: Props = Props[RssiInfoActor]
  case class sendRssiInfo(sceneId : Int, date : Long, hour : Int, visitorNum : Int, repeatNum : Int, macHead : String)
}
