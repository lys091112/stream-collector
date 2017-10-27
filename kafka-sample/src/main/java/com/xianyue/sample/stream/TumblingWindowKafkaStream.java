package com.xianyue.sample.stream;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;

import java.util.Properties;
import java.util.Random;

public class TumblingWindowKafkaStream {

    public static void main(String[] args) throws Exception {

        Properties config = new Properties();

        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "tumbling-window-kafka-streams");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "10.128.6.188:32771");
//        config.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, "localhost:2181");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass().getName());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Long().getClass().getName());

        KStreamBuilder builder = new KStreamBuilder();

        KStream<byte[], Long> longs = builder.stream(Serdes.ByteArray(), Serdes.Long(), "alert-window");

        // The tumbling windows will clear every ten seconds.
        KTable<Windowed<byte[]>, Long> longCounts =
            longs.groupByKey()
                    .count(TimeWindows.of(15000L),
                            "Count");

        // Write to topics.
        longCounts.toStream((k, v) -> k.key())
            .to(Serdes.ByteArray(),
                Serdes.Long(),
                    "alert-window-output");

        KafkaStreams streams = new KafkaStreams(builder, config);
        streams.start();

        // Now generate the data and write to the topic.
        Properties producerConfig = new Properties();
        producerConfig.put("bootstrap.servers", "10.128.6.188:32771");
        producerConfig.put("key.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        producerConfig.put("value.serializer", "org.apache.kafka.common.serialization.LongSerializer");

        KafkaProducer producer = new KafkaProducer<byte[], Long>(producerConfig);

        int total = 0;
        int sum = 0;

        Random rng = new Random(12345L);
        while (total < 220) {
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
