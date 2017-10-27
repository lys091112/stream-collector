package com.xianyue.common.config;

import com.xianyue.common.ConfigPrefix;
import lombok.Data;

@Data
@ConfigPrefix("kafka")
public class CollectorKafkaConfig {
    private String bootstrapServers;
    private String maxBlockMs;
    private String keySerializer;
    private String valueSerializer;
    private String topic;
}
