package com.xianyue.sample.producer;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.producer.Callback;

/**
 * Created by langle on 2017/11/15.
 */
public class RawMetricEventProducer implements Sender<String, AlertEvent>{

    @Override
    public void send(String key, AlertEvent message) throws ExecutionException, InterruptedException {

    }

    @Override
    public void send(String key, AlertEvent message, Callback callback) {

    }

    @Override
    public void close() throws IOException {

    }
}
