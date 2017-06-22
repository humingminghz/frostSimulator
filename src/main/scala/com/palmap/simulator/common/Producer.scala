package com.palmap.simulator.common

import java.util.Properties

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

/**
  * Created by mingming.hu on 2017/6/20.
  */
object Producer {
  val conf: Config = ConfigFactory.load()
  val props = new Properties()
  val topic: String = conf.getString(Common.KAFKA_TOPIC)
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer")
  props.put("bootstrap.servers",conf.getString(Common.KAFKA_HOST))
  props.put("acks", "1")
  props.put("retries", "0")
  props.put("batch.size", "16384")
  props.put("linger.ms", "1")
  props.put("buffer.memory", "33554432")

  val producer = new KafkaProducer[String, Array[Byte]](props)

  def send(key : String, value : Array[Byte]) : Unit={
    producer.send(new ProducerRecord[String, Array[Byte]](topic, key, value))
  }

}
