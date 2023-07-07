package com.sethsaperstein.flinkcontrolapi.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.flink.runtime.rest.util.RestMapperUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguration {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // match mapper from flinksql api so proxy is 1-to-1
        // https://github.com/apache/flink/blob/master/flink-runtime/src/main/java/org/apache/flink/runtime/rest/util/RestMapperUtils.java#L28
        objectMapper.enable(
            DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES,
            DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES,
            DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        return objectMapper;
    }

    @Bean
    public org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper objectMapperShaded() {
        return RestMapperUtils.getStrictObjectMapper();
    }
}
