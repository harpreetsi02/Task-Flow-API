package com.backend.taskflowapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TaskflowapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskflowapiApplication.class, args);
	}

}
