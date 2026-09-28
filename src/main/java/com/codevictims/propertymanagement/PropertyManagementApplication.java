package com.codevictims.propertymanagement;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PropertyManagementApplication {
  public static void main(String[] args) {
    SpringApplication.run(PropertyManagementApplication.class, args);
  }

  @Bean
  Clock clock() {
    return Clock.system(ZoneId.of("Asia/Muscat"));
  }
}
