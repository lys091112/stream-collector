package com.xianyue.sample.producer.callback;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

@Slf4j
public class DefaultCallback<K, V> implements Callback {

    private long startTime;
    private K key;
    private V message;

    public DefaultCallback(long startTime, K key, V message) {
        this.startTime = startTime;
        this.key = key;
        this.message = message;
    }

    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception) {
        if (metadata != null) {
            if (log.isDebugEnabled()) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                System.out.println(
                    "message(" + key + ", " + message + ") sent to partition(" + metadata.partition() + "), "
                        + "offset(" + metadata.offset() + ") in " + elapsedTime + " ms");
            }
        } else {
            log.error("onCompletion error. {}", exception);
        }
    }
}
