package com.xianyue.sample.stream;

import com.xianyue.common.util.TimeUtil;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.kstream.internals.TimeWindow;

/**
 * 用于对数据进行汇聚，抛弃掉旧数据，保证每分钟一条的有序数据队列发送到kafka,
 *
 *  TODO 如何控制发送时间以及发送间隔有待考虑
 */
@Slf4j
public class TumblingWindowKafkaProducer extends KafkaProducer<byte[], byte[]> {

    TreeMap data = new TreeMap<Windowed<String>, ProducerRecord<byte[], byte[]>>(Comparator.comparingLong(o -> o.window().start()));

    private Long windowSize = 60000L;

    private long sendFrequency = windowSize / 3;

    private volatile long sendTime;


    public TumblingWindowKafkaProducer(Map<String, Object> configs, ByteArraySerializer keySerializer,
        ByteArraySerializer valueSerializer) {
        super(configs, keySerializer, valueSerializer);
    }

    @Override
    public Future<RecordMetadata> send(ProducerRecord<byte[], byte[]> record, Callback callback) {
        if (isChangeLog(record.topic())) {
            return super.send(record, callback);
        }

        String key = Serdes.String().deserializer().deserialize(record.topic(), record.key());

        Windowed<String> windowKey = new Windowed(key, new TimeWindow(record.timestamp(), record.timestamp() + windowSize));

        log.info("comming message: key:{}, message:{}, timestamp: {}", record.key(), record.value(),
            TimeUtil.formatTime(record.timestamp()));

        // 数据过期，丢弃
        if (record.timestamp() < sendTime) {
            log.error("delay data!  start:{}, value:{}", record.timestamp(), new LongDeserializer().deserialize(record.topic(), record.value()));
            return null;
        }

        long now = System.currentTimeMillis();

        if (record.timestamp() + sendFrequency < now) {
            data.put(windowKey, record);
            return null;
        }

        Iterator<Map.Entry<Windowed<String>, ProducerRecord<byte[], byte[]>>> iterator = data.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Windowed<String>, ProducerRecord<byte[], byte[]>> entry = iterator.next();
            if (entry.getKey().window().end() <= now) {
                super.send(entry.getValue(), callback);
                sendTime = entry.getKey().window().end();
                log.info("send data! patition:{}, value:{}", record.partition(),
                    new LongDeserializer().deserialize(record.topic(), entry.getValue().value()));
                iterator.remove();
            } else {
                break;
            }
        }

        return null;
    }

    private boolean isChangeLog(String topic) {
        return topic.endsWith("changelog");
    }
}
