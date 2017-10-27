package com.xianyue.common.config;

import com.xianyue.common.ConfigPrefix;
import lombok.Data;

@Data
@ConfigPrefix("consumers")
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
