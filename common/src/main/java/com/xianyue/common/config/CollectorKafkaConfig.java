package com.xianyue.common.config;

import lombok.Data;

@Data
public class CollectorKafkaConfig {
    private String bootstrapServers;
    private String maxBlockMs;
    private String keySerializer;
    private String valueSerializer;
    private String topic;
}
