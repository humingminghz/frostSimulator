package com.palmap.simulator.common

import java.text.SimpleDateFormat
import java.util.Date

/**
  * Created by Administrator on 2017/6/20.
  */
object DateUtil {

  def getDateByString(s : String) : Long ={
    val sdf = new SimpleDateFormat(Common.DAY_FORMAT)
    val day = sdf.parse(s)
    day.getTime
  }

  def getDateByLong(l : Long) : String={
    val sdf = new SimpleDateFormat(Common.DAY_FORMAT)
    sdf.format(new Date(l))
  }

  def getNowDateString: String={
    val sdf = new SimpleDateFormat(Common.DATE_FORMAT)
    sdf.format(new Date())
  }
}
