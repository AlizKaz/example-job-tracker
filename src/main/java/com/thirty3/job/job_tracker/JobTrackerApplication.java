package com.thirty3.job.job_tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class JobTrackerApplication {

  public static void main(String[] args) {
    SpringApplication.run(JobTrackerApplication.class, args);
  }
}
