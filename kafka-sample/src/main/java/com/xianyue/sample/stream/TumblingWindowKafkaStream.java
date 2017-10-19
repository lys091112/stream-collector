package com.xianyue.sample.stream;

import java.util.Properties;
import java.util.Random;
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

public class TumblingWindowKafkaStream {

    public static void main(String[] args) throws Exception {

        Properties config = new Properties();

        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "tumbling-window-kafka-streams");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
//        config.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, "localhost:2181");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass().getName());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Long().getClass().getName());

        KStreamBuilder builder = new KStreamBuilder();

        KStream<byte[], Long> longs = builder.stream(Serdes.ByteArray(), Serdes.Long(), "alert");

        // The tumbling windows will clear every ten seconds.
        KTable<Windowed<byte[]>, Long> longCounts =
            longs.groupByKey()
                .count(TimeWindows.of(10000L)
                        .until(10000L),
                    "alert-output");

        // Write to topics.
        longCounts.toStream((k, v) -> k.key())
            .to(Serdes.ByteArray(),
                Serdes.Long(),
                "Count");

        KafkaStreams streams = new KafkaStreams(builder, config);
        streams.start();

        // Now generate the data and write to the topic.
        Properties producerConfig = new Properties();
        producerConfig.put("bootstrap.servers", "127.0.0.1:9092");
        producerConfig.put("key.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        producerConfig.put("value.serializer",
            "org.apache.kafka.common" +
                ".serialization.LongSerializer");

        KafkaProducer producer =
            new KafkaProducer<byte[], Long>(producerConfig);

        Random rng = new Random(12345L);

        while (true) {
            producer.send(new ProducerRecord<byte[], Long>(
                "alert", "A".getBytes(), rng.nextLong() % 10));
            System.out.println("--------");
            Thread.sleep(500L);
        }
    }
}