package com.xianyue.sample.config;

import com.xianyue.common.config.CollectorConsumerConfig;
import com.xianyue.common.config.CollectorKafkaConfig;
import lombok.Data;

import java.util.List;

@Data
public class SampleConfig {

    private CollectorKafkaConfig kafka;
    private List<CollectorConsumerConfig> consumers;
}
