# Kafka 基础使用

* 创建topic

```sbtshell
sh kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partition 1 --topic alert
```

* 查看kafkaStream处理后的topic内容
```sbtshell
sh kafka-console-consumer.sh \
   --bootstrap-server localhost:9092 \
   --topic "alert-output" \
   --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
```


#### kafka参数

- commit.interval.ms  存储处理器当前位置的间隔毫秒数（即数据发送的频率）
- windowstore.changelog.additional.retention.ms KStream 聚合数据的保存时间，用于处理告警延迟数据重新加入到窗口中


#### 问题记录

- 为何在topic中，kafka stream 消费出来的数据会是两条同时间但是值不一样的两条数据

    因为在kafkaStream中，kStream 和kTable是两种类型， KStream可以认为是记录数据的变化过程，kTable是对数据此时刻的真实景象，保存的永远
    是数据的最终状态值
