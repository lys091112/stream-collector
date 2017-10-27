package com.xianyue.common.config;

import lombok.Data;

@Data
public class CollectorConsumerConfig {
    private String name;
    private String bootstrapServers;
    private String groupId;
    private String autoCommit;
    private String autoCommitIntervalMs;
    private String sessionTimeoutMs;
    private String keySerializer;
    private String valueSerializer;
    private String topic;
}
