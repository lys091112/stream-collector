package com.xianyue.sample.stream;

import java.util.Map;
import java.util.Properties;
import java.util.Random;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.kstream.internals.WindowedDeserializer;
import org.apache.kafka.streams.kstream.internals.WindowedSerializer;
import org.apache.kafka.streams.state.WindowStore;

public class TumblingWindowKafkaStream {

    public static void main(String[] args) throws Exception {

        long windowSize = 60000L;

        Properties config = new Properties();

        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "tumbling-window-kafka-streams");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "10.128.6.188:32769");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Long().getClass().getName());
        config.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10000L);

        KStreamBuilder builder = new KStreamBuilder();
        KStream<String, Long> longs = builder.stream(Serdes.String(), Serdes.Long(), "alert-window3");

        // The tumbling windows will clear every ten seconds.
        KTable<Windowed<String>, Long> longCounts =
            longs.groupByKey().windowedBy(TimeWindows.of(windowSize))
                .count(Materialized.<String, Long, WindowStore<Bytes, byte[]>>as("Count3"));

        WindowedSerializer<String> windowedSerializer = new WindowedSerializer<>(Serdes.String().serializer());
        WindowedDeserializer<String> windowedDeserializer = new WindowedDeserializer<>(
            Serdes.String().deserializer(), windowSize);
        Serde<Windowed<String>> windowedSerde = Serdes.serdeFrom(windowedSerializer, windowedDeserializer);

        longCounts.toStream().map((key, value) -> new KeyValue<>(key, value)).to("alert-window3-output", Produced.with(windowedSerde, Serdes.Long()));

        System.out.println("-----------------> " + builder.nodeGroups());
        KafkaStreams streams = new KafkaStreams(builder, new StreamsConfig(config), new WindowKafkaClientSupplier());
        streams.start();

    }

    public static void TumblinProducer(Map<String, Long> keyValues) throws InterruptedException {
        Properties producerConfig = new Properties();
        producerConfig.put("bootstrap.servers", "10.128.6.188:32769");
        producerConfig.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerConfig.put("value.serializer", "org.apache.kafka.common.serialization.LongSerializer");

        KafkaProducer producer = new KafkaProducer<String, Long>(producerConfig);

        Random rng = new Random(12345L);
        int total = 0;
        while (total < 2200) {
            long num = rng.nextLong() % 10;
            producer.send(new ProducerRecord<String, Long>("alert-window3", "A", num));
            System.out.println("-------- " + total++);
            Thread.sleep(500L);
            if (total % 60 == 0) {
                System.out.println("------keyValues----------->>" + keyValues);
            }
        }
    }
}
