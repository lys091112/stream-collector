package com.xianyue.sample.stream;

import java.util.Map;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.streams.processor.internals.DefaultKafkaClientSupplier;

public class WindowKafkaClientSupplier extends DefaultKafkaClientSupplier {

    @Override
    public Producer<byte[], byte[]> getProducer(Map<String, Object> config) {
        return new TumblingWindowKafkaProducer(config, new ByteArraySerializer(), new ByteArraySerializer());
    }


}
