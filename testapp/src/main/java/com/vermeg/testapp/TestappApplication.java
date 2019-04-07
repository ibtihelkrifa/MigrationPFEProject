package com.vermeg.testapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import scalaclasses.GreetingInScala;

@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement

public class TestappApplication {

	public static void main(String[] args) {

		SpringApplication.run(TestappApplication.class, args);

	}

}
