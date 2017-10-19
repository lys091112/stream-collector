package com.xianyue.sample.config;

import lombok.Data;

@Data
public class SampleConfig {

    private KafkaConfig kafka;
    private ZookeeperConfig zookeeper;

    @Data
    public class KafkaConfig {

        private String bootstrapServers;
        private String maxBlockMs;
        private String keySerializer;
        private String valueSerializer;
        private String topic;
    }

    @Data
    public class ZookeeperConfig {

        private String servers;
    }

}
