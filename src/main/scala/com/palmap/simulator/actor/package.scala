package com.palmap.simulator

import com.palmap.simulator.common.Common
import com.typesafe.config.{Config, ConfigFactory}

/**
  * Created by Administrator on 2017/6/20.
  */
package object actor {
  val config: Config = ConfigFactory.load()
  var id: Int = config.getInt(Common.BEGIN_ID)
}
