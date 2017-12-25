/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xianyue.sample.consumer;

import com.xianyue.common.config.CollectorConsumerConfig;
import com.xianyue.common.util.TimeUtil;
import com.xianyue.common.util.YamlConverter;
import com.xianyue.sample.config.SampleConfig;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import kafka.utils.ShutdownableThread;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.kstream.internals.WindowedDeserializer;

public class Consumer<K,V> extends ShutdownableThread {
    private final KafkaConsumer<K, V> consumer;
    private final CollectorConsumerConfig config;

    public Consumer(CollectorConsumerConfig config) {
        super(config.getName(), false);
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getGroupId());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, config.getAutoCommit());
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, config.getAutoCommitIntervalMs());
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, config.getSessionTimeoutMs());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, config.getKeySerializer());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, config.getValueSerializer());

        consumer = new KafkaConsumer<K, V>(props,
            (Deserializer<K>) new WindowedDeserializer<byte[]>(Serdes.ByteArray().deserializer(), 10000L), null);
        this.config = config;
    }

    @Override
    public void doWork() {
        consumer.subscribe(Collections.singletonList(this.config.getTopic()));
        ConsumerRecords<K, V> records = consumer.poll(1000);
        for (ConsumerRecord<K, V> record : records) {
            System.out.println("Received message: (" + record.value() + ") at offset " + record.offset() + " time: " +
                record.key() + ", time: " + TimeUtil.formatTime(record.timestamp()));
        }
    }

    @Override
    public String name() {
        return config.getName();
    }

    @Override
    public boolean isInterruptible() {
        return false;
    }

    public static void main(String[] args) throws IOException {
        SampleConfig config = YamlConverter.config(SampleConfig.class, "application.yml");
        Consumer<Windowed<byte[]>, Long> consumer = new Consumer<>(config.getConsumers().get(0));
        new Thread(consumer).start();
    }
}
