package com.sethsaperstein.flinkcontrolapi;

import com.sethsaperstein.flinkcontrolapi.config.JacksonConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@ComponentScan("com.sethsaperstein.flinkcontrolapi")
@Import(JacksonConfiguration.class)
public class FlinkControlApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlinkControlApiApplication.class, args);
	}
}
