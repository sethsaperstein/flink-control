package com.sethsaperstein.flinkcontrolapi;

import com.sethsaperstein.flinkcontrolapi.config.JacksonConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Configuration
@ComponentScan("com.sethsaperstein.flinkcontrolapi")
@Import(JacksonConfiguration.class)
public class TestConfiguration {
    @Bean
    public int port() {
        return 8081; // anything but 8080
    }
}
