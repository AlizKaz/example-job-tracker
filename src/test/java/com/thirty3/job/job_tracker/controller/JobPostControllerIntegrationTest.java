package com.thirty3.job.job_tracker.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.thirty3.job.job_tracker.controller.dto.CreateJobPost;
import com.thirty3.job.job_tracker.model.JobPost;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JobPostControllerIntegrationTest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void getJobPostShouldReturn404() {
    assertThat(
        this.restTemplate
            .getForEntity("http://localhost:" + port + "/job-post", String.class)
            .getStatusCode()
            .is4xxClientError());
  }

  @Test
  void createJobPostShouldReturn404() {
    assertThat(
        this.restTemplate
            .postForEntity(
                "http://localhost:" + port + "/job-post", null, String.class, (Object) null)
            .getStatusCode()
            .is4xxClientError());
  }

  @Test
  void createJobPostShouldReturnOK() {
    CreateJobPost request = CreateJobPost.builder().jobTitle("<job title>").build();
    ResponseEntity<JobPost> responseEntity =
        this.restTemplate.postForEntity(
            "http://localhost:" + port + "/job-post", request, JobPost.class, (Object) null);
    assertThat(responseEntity.getStatusCode().is2xxSuccessful());
    JobPost post = responseEntity.getBody();
    System.out.println("post = " + post);
    assertThat(post == null);
    assertThat(post.getId() >= 1);
  }
}
