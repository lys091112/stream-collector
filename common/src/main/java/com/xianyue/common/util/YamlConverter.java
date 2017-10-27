package com.xianyue.common.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.xianyue.common.ConfigPrefix;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class YamlConverter<T> {

    private YAMLFactory yamlFactory;
    private ObjectMapper mapper;
    private Class<T> clazz;

    YamlConverter(Class<T> clazz) {
        this.clazz = clazz;
        this.yamlFactory = new YAMLFactory();
        this.mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public T build(String path) throws IOException {
        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(path)) {
            if(null == input) {
                log.error("can't find config file! filePath: {}", path);
                throw new FileNotFoundException();
            }

            YAMLParser yamlParser = yamlFactory.createParser(input);
            JsonNode node = mapper.readTree(yamlParser);
            if (this.clazz.isAnnotationPresent(ConfigPrefix.class)) {
                String prefix = this.clazz.getAnnotation(ConfigPrefix.class).value();
                node = node.get(prefix);
            }

            if (null == node) {
                log.error("there's no config with class :{}, please check the config file", clazz.getName());
                throw new RuntimeException("invaild config file");
            }

            TreeTraversingParser treeTraversingParser = new TreeTraversingParser(node);
            final T config = mapper.readValue(treeTraversingParser, clazz);
            return config;
        } catch (Exception e) {
            throw e;
        }
    }

    public static <T> T config(Class<T> clazz, String configPath) throws IOException {
        return new YamlConverter<T>(clazz).build(configPath);
    }

}
