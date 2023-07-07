package com.sethsaperstein.flinkcontrolapi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ReverseProxyFilterTest {
    @Test
    public void testConstructTargetUrl() {
        String requestUri = "/sql/integ/v1/sessions";
        String expectedTargetUrl = "http://integ-sql-gateway.integ:8080/v1/sessions";

        String actualTargetUrl = ReverseProxyFilter.constructTargetUrl(requestUri);

        Assertions.assertEquals(expectedTargetUrl, actualTargetUrl);
    }

    @Test
    public void testConstructTargetUrlWithInvalidRequestUri() {
        String requestUri = "/sql/v1/sessions"; // Invalid request URI without cluster name
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ReverseProxyFilter.constructTargetUrl(requestUri);
        });

        String expectedMessage = "Invalid request URI. Cluster name cannot be extracted.";
        Assertions.assertEquals(expectedMessage, exception.getMessage());
    }
}
