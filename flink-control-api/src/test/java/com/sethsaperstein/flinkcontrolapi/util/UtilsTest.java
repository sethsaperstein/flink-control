package com.sethsaperstein.flinkcontrolapi.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
}
