package com.xianyue.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.xianyue.common.ConfigPrefix;
import com.xianyue.common.config.CollectorConsumerConfig;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * it just a demo
 */
@Deprecated
public class YamlListConverter<T> {

    private YAMLFactory yamlFactory;
    private ObjectMapper mapper;
    private Class<T> clazz;

    YamlListConverter(Class<T> clazz) {
        this.clazz = clazz;
        this.yamlFactory = new YAMLFactory();
        this.mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public List<T> build(String path, TypeReference<List<T>> typeReference) throws IOException {
        try {
            InputStream input = this.getClass().getClassLoader().getResourceAsStream(path);
            YAMLParser yamlParser = yamlFactory.createParser(input);
            JsonNode node = mapper.readTree(yamlParser);
            if (this.clazz.isAnnotationPresent(ConfigPrefix.class)) {
                String prefix = this.clazz.getAnnotation(ConfigPrefix.class).value();
                node = node.get(prefix);
            }

            System.out.println(node);

            TreeTraversingParser treeTraversingParser = new TreeTraversingParser(node);
            return mapper.readValue(treeTraversingParser, typeReference);
        } catch (Exception e) {
            throw e;
        }
    }

    public static <T> List<T> config(Class<T> clazz, TypeReference<List<T>> typeReference, String configPath)
        throws IOException {
        return new YamlListConverter<T>(clazz).build(configPath, typeReference);
    }

    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String key = "[{\"name\":\"KafkaConsumerExample\",\"bootstrapServers\":\"10.128.6.188:32771\","
            + "\"groupId\":\"sampleConsumer\",\"autoCommit\":true,\"autoCommitIntervalMs\":10000,"
            + "\"sessionTimeoutMs\":30000,\"keySerializer\":\"org.apache.kafka.common.serialization"
            + ".StringSerializer\",\"valueSerializer\":\"org.apache.kafka.common.serialization.StringSerializer\","
            + "\"topic\":\"alert\"}]";

        List<CollectorConsumerConfig> configs = objectMapper
            .readValue(key, new TypeReference<List<CollectorConsumerConfig>>() {
            });
        System.out.println(configs);
    }

}
