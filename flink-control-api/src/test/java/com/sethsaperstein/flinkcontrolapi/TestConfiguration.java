package com.sethsaperstein.flinkcontrolapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.sethsaperstein.flinkcontrolapi")
public class TestConfiguration {
    @Bean
    public int port() {
        return 8081; // anything but 8080
    }
}
