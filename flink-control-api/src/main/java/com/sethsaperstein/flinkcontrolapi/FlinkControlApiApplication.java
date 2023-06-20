package com.sethsaperstein.flinkcontrolapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.sethsaperstein.flinkcontrolapi")
public class FlinkControlApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlinkControlApiApplication.class, args);
	}
}
