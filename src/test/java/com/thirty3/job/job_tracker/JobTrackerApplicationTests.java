package com.thirty3.job.job_tracker;

import static org.assertj.core.api.Assertions.assertThat;

import com.thirty3.job.job_tracker.controller.JobPostController;
import com.thirty3.job.job_tracker.repository.JobPostRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class JobTrackerApplicationTests {
  @Autowired private JobPostController jobPostController;
  @Autowired private JobPostRepository jobPostRepository;

  @Test
  void contextLoads() {
    assertThat(jobPostController).isNotNull();
    assertThat(jobPostRepository).isNotNull();
  }
}
