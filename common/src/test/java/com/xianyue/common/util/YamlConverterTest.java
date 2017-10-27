package com.xianyue.common.util;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import com.xianyue.common.config.CollectorKafkaConfig;
import java.io.IOException;
import org.junit.Test;

public class YamlConverterTest {

    @Test
    public void config() throws IOException {
        CollectorKafkaConfig kafkaConfig = YamlConverter.config(CollectorKafkaConfig.class, "application.yml");
        assertThat(kafkaConfig.getBootstrapServers(), equalTo("10.128.6.188:32771"));
        assertThat(kafkaConfig.getMaxBlockMs(), equalTo("5000"));
        assertThat(kafkaConfig.getKeySerializer(), equalTo("org.apache.kafka.common.serialization.StringSerializer"));
        assertThat(kafkaConfig.getValueSerializer(), equalTo("org.apache.kafka.common.serialization.StringSerializer"));
        assertThat(kafkaConfig.getTopic(), equalTo("alert"));
    }

}