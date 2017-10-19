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
