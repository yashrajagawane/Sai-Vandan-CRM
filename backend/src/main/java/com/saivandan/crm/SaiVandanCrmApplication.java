package com.saivandan.crm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SaiVandanCrmApplication {
  public static void main(String[] args) { SpringApplication.run(SaiVandanCrmApplication.class, args); }
}

