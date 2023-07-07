package com.sethsaperstein.flinkcontrolapi.util;

import com.sethsaperstein.flinkcontrolapi.config.JacksonConfiguration;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.table.gateway.rest.message.session.OpenSessionRequestBody;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = JacksonConfiguration.class)
public class UtilsTest {

    @Test
    public void testGenerateRandomName() {
        String generatedName = Utils.generateRandomName();

        // Assert that the generated name adheres to the rules
        Assertions.assertTrue(generatedName.matches("^[a-z][a-z0-9-]{0,43}[a-z0-9]$"),
            "Generated name should match the specified pattern");

        // Assert that the generated name is within the length limit
        Assertions.assertTrue(generatedName.length() <= 45,
            "Generated name should be no more than 45 characters");
    }

    @Test
    public void testSerialization() throws org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException {
        OpenSessionRequestBody body = new OpenSessionRequestBody(null, null);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(body);
        OpenSessionRequestBody deserializedBody = objectMapper.readValue(json, OpenSessionRequestBody.class);
        System.out.println("Deserialized body: " + deserializedBody);
    }
}
