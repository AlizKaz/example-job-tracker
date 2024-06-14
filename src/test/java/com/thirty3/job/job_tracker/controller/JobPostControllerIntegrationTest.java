package com.thirty3.job.job_tracker.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thirty3.job.job_tracker.controller.dto.JobPostCreateRequest;
import com.thirty3.job.job_tracker.controller.dto.JobPostUpdateRequest;
import com.thirty3.job.job_tracker.model.JobPost;
import com.thirty3.job.job_tracker.repository.JobPostRepository;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JobPostControllerIntegrationTest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private JobPostRepository repository;

  @BeforeEach
  public void initTests() {
    repository.deleteAll();

    // this hack is for PATCHing with rest template:
    // https://stackoverflow.com/questions/29447382/resttemplate-patch-request/29803488#29803488
    restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
  }

  @Test
  void Given_NoJobPost_When_GetJobPost_ThenClientError() {
    assertThat(
        this.restTemplate
            .getForEntity("http://localhost:" + port + "/job-post/10", String.class)
            .getStatusCode()
            .is4xxClientError());
  }

  @Test
  void Given_JobPostExists_When_GetJobPost_ThenReturnJobPost() {
    // Arrange
    long jobPostId = createAJobPost("Senior Software Developer").getId();

    // Action
    ResponseEntity<JobPost> re =
        this.restTemplate.getForEntity(
            "http://localhost:" + port + "/job-post/" + jobPostId, JobPost.class);
    assertThat(re.getStatusCode().is2xxSuccessful()).as("http status code");
    assertThat(re.getBody()).as("body should not be null").isNotNull();
    assertThat(re.getBody().getJobTitle())
        .as("jobPost's job title")
        .isEqualTo("Senior Software Developer");
  }

  @Test
  void Given_EmptyRequestBody_When_PostJobPost_ThenReturnClientError() {
    JobPostCreateRequest request = null;
    assertThat(
        this.restTemplate
            .postForEntity(
                "http://localhost:" + port + "/job-post", request, String.class, (Object) null)
            .getStatusCode()
            .is4xxClientError());
  }

  @Test
  void Given_ValidJobPost_WhenCreateJobPost_ThenGetJobPostShouldReturnJobPost() {
    // Action
    JobPostCreateRequest request = JobPostCreateRequest.builder().jobTitle("<job title>").build();
    ResponseEntity<JobPost> responseEntity =
        this.restTemplate.postForEntity(
            "http://localhost:" + port + "/job-post", request, JobPost.class, (Object) null);

    assertThat(responseEntity.getBody()).as("body").isNotNull();
    var jobPostId = responseEntity.getBody().getId();

    // Assert
    JobPost jobPost = getJobPost(jobPostId);
    assertThat(jobPost.getId()).as("id").isEqualTo(jobPostId);
    assertThat(jobPost.getJobTitle()).as("job title").isEqualTo("<job title>");
  }

  @Test
  void Given_ValidJobPost_WhenCreateJobPost_ThenJobPostShouldBeCreatedWithBookmarkedStatus() {
    // Action
    JobPostCreateRequest request = JobPostCreateRequest.builder().jobTitle("<job title>").build();
    ResponseEntity<JobPost> responseEntity =
        this.restTemplate.postForEntity(
            "http://localhost:" + port + "/job-post", request, JobPost.class, (Object) null);

    assertThat(responseEntity.getBody()).as("body").isNotNull();
    var jobPostId = responseEntity.getBody().getId();

    // Assert
    JobPost jobPost = getJobPost(jobPostId);
    assertThat(jobPost.getStatus()).as("id").isEqualTo(JobPost.Status.BOOKMARKED);
  }

  @Test
  void Given_1000JobPostInDB_WhenGetAllJobPost_ThenShouldReturn1000JobPosts()
      throws JsonProcessingException {
    // Arrange
    for (int i = 0; i < 1000; i++) {
      createAJobPost("random title_" + new Date().getTime());
    }

    // Action

    String jsonAsString =
        this.restTemplate.getForObject(
            "http://localhost:" + port + "/job-post", String.class, (Object) null);
    List<JobPost> list =
        new ObjectMapper().readValue(jsonAsString, new TypeReference<List<JobPost>>() {});

    assertThat(list).as("job post list size does not match expected").hasSize(1000);
  }

  @Test
  void Given_ValidUpdateJobPostRequest_WhenUpdateJobPost_ThenJobPostShouldBeUpdated() {
    // Arrange
    JobPost jobPost =
        createAJobPost(
            "originalTitle",
            "originalUrl",
            "originalJobDescription",
            "originalCompanyName",
            "originalLocation",
            JobPost.Status.BOOKMARKED);

    // Action
    var jobPostUpdateRequest =
        JobPostUpdateRequest.builder()
            .jobTitle("patchedTitle")
            .url("patchedUrl")
            .jobDescription("patchedJobDescription")
            .companyName("patchedCompanyName")
            .location("patchedLocation")
            .status(JobPost.Status.ACCEPTED)
            .build();

    this.restTemplate.patchForObject(
        "http://localhost:" + port + "/job-post/" + jobPost.getId(),
        jobPostUpdateRequest,
        JobPost.class);

    // Assert
    var updatedJobPost = getJobPost(jobPost.getId());
    assertThat(updatedJobPost.getId()).as("job post id").isEqualTo(jobPost.getId());
    assertThat(updatedJobPost.getJobTitle()).as("job post title").isEqualTo("patchedTitle");
    assertThat(updatedJobPost.getJobDescription())
        .as("job post description")
        .isEqualTo("patchedJobDescription");
    assertThat(updatedJobPost.getUrl()).as("job post url").isEqualTo("patchedUrl");
    assertThat(updatedJobPost.getCompanyName())
        .as("job post company name")
        .isEqualTo("patchedCompanyName");
    assertThat(updatedJobPost.getLocation()).as("job post location").isEqualTo("patchedLocation");
    assertThat(updatedJobPost.getStatus()).as("job post status").isEqualTo(JobPost.Status.ACCEPTED);
  }

  @Test
  void Given_NullValueForUrl_WhenUpdateJobPost_ThenFieldShouldNotUpdate() {
    // Arrange
    JobPost jobPost =
        createAJobPost(
            "originalTitle",
            "originalUrl",
            "originalJobDescription",
            "originalCompanyName",
            "originalLocation",
            JobPost.Status.BOOKMARKED);

    // Action
    JobPostUpdateRequest jobPostToBePatched =
        JobPostUpdateRequest.builder().jobTitle("originalTitle").url(null).build();

    this.restTemplate.patchForObject(
        "http://localhost:" + port + "/job-post/" + jobPost.getId(),
        jobPostToBePatched,
        JobPost.class);

    // Assert
    var updatedJobPost = getJobPost(jobPost.getId());
    assertThat(updatedJobPost.getId()).as("job post id").isEqualTo(jobPost.getId());
    assertThat(updatedJobPost.getUrl()).as("job post url").isEqualTo("originalUrl");
  }

  @Test
  void Given_JobPostExists_WhenDeleteJobPost_ThenGetJobPostShouldReturnNotFound() {
    // Arrange
    JobPost jobPost = createAJobPost("job title");

    // Action
    this.restTemplate.delete("http://localhost:" + port + "/job-post/" + jobPost.getId());

    // Assert
    assertThat(
            this.restTemplate
                .getForEntity(
                    "http://localhost:" + port + "/job-post/" + jobPost.getId(), String.class)
                .getStatusCode())
        .isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void Given_JobPostDoesNotExist_WhenDeleteJobPost_ThenShouldReturnNotFound() {
    // Action
    System.out.println(
        this.restTemplate
            .exchange(
                "http://localhost:" + port + "/job-post/" + 10,
                HttpMethod.DELETE,
                null,
                String.class)
            .getStatusCode());
    assertThat(
            this.restTemplate
                .exchange(
                    "http://localhost:" + port + "/job-post/" + 10,
                    HttpMethod.DELETE,
                    null,
                    String.class)
                .getStatusCode())
        .as("status code")
        .isEqualTo(HttpStatus.NOT_FOUND);
  }

  // utils

  private JobPost createAJobPost(
      String jobTitle,
      String url,
      String jobDescription,
      String companyName,
      String location,
      JobPost.Status status) {
    JobPostCreateRequest request =
        JobPostCreateRequest.builder()
            .jobTitle(jobTitle)
            .url(url)
            .jobDescription(jobDescription)
            .companyName(companyName)
            .location(location)
            .status(status)
            .build();
    ResponseEntity<JobPost> jobPostResponseEntity =
        this.restTemplate.postForEntity(
            "http://localhost:" + port + "/job-post", request, JobPost.class, (Object) null);

    return jobPostResponseEntity.getBody();
  }

  private JobPost createAJobPost(String jobTitle) {
    JobPostCreateRequest request = JobPostCreateRequest.builder().jobTitle(jobTitle).build();
    ResponseEntity<JobPost> jobPostResponseEntity =
        this.restTemplate.postForEntity(
            "http://localhost:" + port + "/job-post", request, JobPost.class, (Object) null);

    return jobPostResponseEntity.getBody();
  }

  private JobPost getJobPost(long jobPostId) {
    return this.restTemplate.getForObject(
        "http://localhost:" + port + "/job-post/" + jobPostId, JobPost.class);
  }
}
