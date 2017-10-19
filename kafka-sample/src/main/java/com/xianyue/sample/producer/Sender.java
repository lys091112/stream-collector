package com.xianyue.sample.producer;

import java.io.Closeable;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.producer.Callback;

public interface Sender<K, V> extends Closeable {

    void send(K key, V message) throws ExecutionException, InterruptedException;

    void send(K key, V message, Callback callback);

}
