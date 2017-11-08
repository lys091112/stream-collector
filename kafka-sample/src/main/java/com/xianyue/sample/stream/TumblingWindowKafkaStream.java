package com.xianyue.sample.stream;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.kstream.internals.WindowedDeserializer;
import org.apache.kafka.streams.kstream.internals.WindowedSerializer;

import java.util.Properties;
import java.util.Random;

public class TumblingWindowKafkaStream {

    public static void main(String[] args) throws Exception {

        Properties config = new Properties();

        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "tumbling-window-kafka-streams");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "10.128.6.188:32770");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass().getName());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Long().getClass().getName());
//        config.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 30000L);
//        config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);

        KStreamBuilder builder = new KStreamBuilder();

        KStream<byte[], Long> longs = builder.stream(Serdes.ByteArray(), Serdes.Long(), "alert-window");

        // The tumbling windows will clear every ten seconds.
        KTable<Windowed<byte[]>, Long> longCounts =
                longs.groupByKey()
                        .count(TimeWindows.of(10000L).until(10000L),
                                "Count");


        WindowedSerializer<byte[]> windowedSerializer = new WindowedSerializer<>(Serdes.ByteArray().serializer());
        WindowedDeserializer<byte[]> windowedDeserializer = new WindowedDeserializer<>(Serdes.ByteArray().deserializer(), 10000L);
        Serde<Windowed<byte[]>> windowedSerde = Serdes.serdeFrom(windowedSerializer, windowedDeserializer);

        // need to override key serde to Windowed<byte[]> type
        longCounts.toStream().map(new KeyValueMapper<Windowed<byte[]>, Long, KeyValue<Windowed<byte[]>, Long>>() {
                                      @Override
                                      public KeyValue<Windowed<byte[]>, Long> apply(Windowed<byte[]> key, Long value) {
                                          return new KeyValue<>(key, value);
                                      }
                                  }
        ).filter(new Predicate<Windowed<byte[]>, Long>() {
            // 不保留中间记录，将没有完成的窗口数据guo lu过滤掉，只发送已经完全聚合过的数据，超过该窗口的数据则进行丢弃
            @Override
            public boolean test(Windowed<byte[]> key, Long value) {
                return key.window().end() <= System.currentTimeMillis();
            }
        }).to("alert-window-output", Produced.with(windowedSerde, Serdes.Long()));

        KafkaStreams streams = new KafkaStreams(builder, config);
        streams.start();

        // Now generate the data and write to the topic.
        Properties producerConfig = new Properties();
        producerConfig.put("bootstrap.servers", "10.128.6.188:32770");
        producerConfig.put("key.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        producerConfig.put("value.serializer", "org.apache.kafka.common.serialization.LongSerializer");
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, 1);

        KafkaProducer producer = new KafkaProducer<byte[], Long>(producerConfig);

        int total = 0;
        int sum = 0;

        ProducerRecord producerRecord = new ProducerRecord<byte[], Long>("alert-window", "A".getBytes(), -1L);

        Random rng = new Random(12345L);
        while (total < 2200) {
            long num = rng.nextLong() % 10;
            producer.send(new ProducerRecord<byte[], Long>("alert-window", "A".getBytes(), num));
            System.out.println("-------- " + total++);
            Thread.sleep(500L);
            sum += num;
            if (total % 30 == 0) {
                System.out.println("---- once-------------->> sum = " + sum);
                sum = 0;
            }
        }
    }
}
