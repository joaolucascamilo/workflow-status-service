package com.ocorrencia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class WorkflowStatusServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkflowStatusServiceApplication.class, args);
	}

}
