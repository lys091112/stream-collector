package com.xianyue.sample.producer;

import com.xianyue.sample.config.SampleConfig.KafkaConfig;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

@Slf4j
public class SampleProducer<K, V> implements Sender<K, V> {

    private KafkaConfig config;
    private Producer<K, V> producer;

    public SampleProducer(KafkaConfig config) {
        this.config = config;
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            config.getBootstrapServers());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "SampleProducer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, config.getKeySerializer());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, config.getValueSerializer());
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, config.getMaxBlockMs());  //超时时间
        this.producer = new KafkaProducer<>(props);
    }

    @Override
    public void close() {
        if (this.producer != null) {
            this.producer.flush();
            this.producer.close();
        }
    }

    @Override
    public void send(K key, V message) throws ExecutionException, InterruptedException {
        long startTime = 0L;
        if (log.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
        }
        final ProducerRecord<K, V> record =
            new ProducerRecord<>(config.getTopic(), key, message);
        RecordMetadata metadata = this.producer.send(record).get();

        if (log.isDebugEnabled()) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            System.out.printf("sent record(key=%s value=%s) " +
                    "meta(partition=%d, offset=%d) time=%d\n",
                record.key(), record.value(), metadata.partition(),
                metadata.offset(), elapsedTime);
        }
    }

    @Override
    public void send(K key, V message, Callback callback) {
        final ProducerRecord<K, V> record =
            new ProducerRecord<>(config.getTopic(), key, message);
        this.producer.send(record, callback);
    }
}
