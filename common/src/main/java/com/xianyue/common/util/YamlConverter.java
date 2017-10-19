package com.xianyue.common.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import java.io.IOException;
import java.io.InputStream;

public class YamlConverter<T> {

    private YAMLFactory yamlFactory;
    private ObjectMapper mapper;
    private Class<T> clazz;

    YamlConverter(Class<T> clazz) {
        this.clazz = clazz;
        this.yamlFactory = new YAMLFactory();
        this.mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public T build(String path) throws IOException {
        try {
            InputStream input = this.getClass().getClassLoader().getResourceAsStream(path);
            YAMLParser yamlParser = yamlFactory.createParser(input);
//            final JsonNode node = mapper.readTree(yamlParser);
//            TreeTraversingParser treeTraversingParser = new TreeTraversingParser(node);
            final T config = mapper.readValue(yamlParser, clazz);
            return config;
        } catch (Exception e) {
            throw e;
        }
    }

    public static <T> T config(Class<T> clazz, String configPath) throws IOException {
        return new YamlConverter<T>(clazz).build("application.yml");
    }

}
