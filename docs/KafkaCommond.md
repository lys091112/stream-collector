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


#### kafka stream 类

- DefaultKafkaClientSupplier 可以用于自定义发送producer类，用于检测发送数据


#### 问题记录

- 为何在topic中，kafka stream 消费出来的数据会是两条同时间但是值不一样的两条数据

    因为在kafkaStream中，kStream 和kTable是两种类型， KStream可以认为是记录数据的变化过程，kTable是对数据此时刻的真实景象，保存的永远
    是数据的最终状态值
    
    试探一： 保证Kafka Topic 中的数据唯一，那么就要保证没有聚合完的数据不能发送到kafka
    解决方法： 使用filter(Windowed<T>) 来过滤掉窗口没有聚合完成的数据
    
    ```java
      public class Test {
        public void test(KStream kstream) {
         kstream.filter(new Predicate<Windowed<byte[]>, Long>() {
              // prefect
              // 不保留中间记录，将没有完成的窗口数据guo lu过滤掉，只发送已经完全聚合过的数据，超过该窗口的数据则进行丢弃
              @Override
              public boolean test(Windowed<byte[]> key, Long value) {
                  return key.window().end() <= System.currentTimeMillis();
              }
          });
        }
    }
    ```
    但是这有一个致命的问题，就是如果在没到达窗口临界点前最后一个点到达，那么到临界点时，没有点数据进行更新ktable，就会造成这一跳数据的丢失,
    所以这种方式是不可行的
    
    考虑到流的特性，产生这种现象的原因是：对于流数据处理，往往是触发式的，来一条数据那么他会从流处理的开头知道流处理的结束，那么他会
    产生一条数据变更记录，由于可能会有数据延迟的影响，那么会产生过期数据的更新，此时会多一条Kstream数据，以及KTable的一个记录更新
    
    如果想要做到每个窗口一条流数据，那么必须保证知道这个窗口结束，才发送这条数据，那么对于这条数据的处理则无法使用流进行处理，反而应该用
    类似于定时任务一样的功能，但这样就无法使用KStream， 
    
    **WAY 2**： 为保证数据的准确性，去除Kafka中每个窗口一条数据这个限制，在消费的时候使用kTable进行消费，然后用KTable中的数据
    更新本地的数据，保证数据的最终准确性
    
    **WAY 3**: TumblingWindowKafkaProducer 通过在本地缓存数据，并剔除掉过时的旧数据，保证发送到kafka的数据是单调有序的
    
    
