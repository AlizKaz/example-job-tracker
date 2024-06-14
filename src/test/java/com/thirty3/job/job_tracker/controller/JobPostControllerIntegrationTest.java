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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.MultiValueMap;

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

  @Nested
  class GetJobPost {
    @DisplayName("get job post should return status code not found if there's no job with the id")
    @Test
    void Given_JobPostDoesNotExists_When_GetJobPost_ThenHttpNotFound() {
      assertThat(
              restTemplate
                  .getForEntity("http://localhost:" + port + "/job-post/10", String.class)
                  .getStatusCode())
          .as("status code")
          .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @DisplayName(
        "get job post should return the job post object when job post id is found in database")
    @Test
    void Given_JobPostExists_When_GetJobPost_ThenReturnJobPost() {
      // Arrange
      long jobPostId = createAJobPost("Senior Software Developer").getId();

      // Action
      JobPost jobPost =
          restTemplate.getForObject(
              "http://localhost:" + port + "/job-post/" + jobPostId, JobPost.class);
      assertThat(jobPost.getJobTitle())
          .as("body should not be null")
          .as("jobPost's job title")
          .isEqualTo("Senior Software Developer");
    }
  }

  @Nested
  class CreateJobPostPost {
    @DisplayName("Create job post when body is empty should return Http status bad request")
    @Test
    void Given_EmptyRequestBody_When_CreateJobPost_ThenReturnBadRequest() {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

      assertThat(
              restTemplate
                  .postForEntity("http://localhost:" + port + "/job-post", request, String.class)
                  .getStatusCode())
          .as("status code")
          .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @DisplayName("Create Job Post should persist all the fields")
    @Test
    void Given_JobPost_WhenCreateJobPost_ThenGetJobPostShouldReturnJobPost() {
      // Action
      var id =
          createAJobPost(
                  "Senior Software Developer",
                  "https://company.com/url",
                  "Ipsum Lorel",
                  "Uber",
                  "Toronto - Canada",
                  null)
              .getId();

      // Assert
      var jobPost = getJobPost(id);
      assertThat(jobPost.getId()).as("id").isEqualTo(id);
      assertThat(jobPost.getJobTitle()).as("job title").isEqualTo("Senior Software Developer");
      assertThat(jobPost.getUrl()).as("url").isEqualTo("https://company.com/url");
      assertThat(jobPost.getJobDescription()).as("job description").isEqualTo("Ipsum Lorel");
      assertThat(jobPost.getLocation()).as("location").isEqualTo("Toronto - Canada");
      assertThat(jobPost.getCreatedDate()).as("createdDate").isNotNull();
    }

    @DisplayName("default status for job post creation is Bookmarked")
    @Test
    void Given_JobPost_WhenCreateJobPost_ThenJobPostShouldBeCreatedWithBookmarkedStatus() {
      // Action
      JobPostCreateRequest request =
          JobPostCreateRequest.builder().jobTitle("Senior Software Engineer").build();
      var jobPostId =
          restTemplate
              .postForObject("http://localhost:" + port + "/job-post", request, JobPost.class)
              .getId();

      // Assert
      JobPost jobPost = getJobPost(jobPostId);
      assertThat(jobPost.getStatus()).as("status").isEqualTo(JobPost.Status.BOOKMARKED);
    }
  }

  @Nested
  class GetAllJobPosts {
    @DisplayName("get all job posts should return all the job posts in the database")
    @Test
    void Given_1000JobPostInDB_WhenGetAllJobPost_ThenShouldReturn1000JobPosts()
        throws JsonProcessingException {
      // Arrange
      for (int i = 0; i < 1000; i++) {
        createAJobPost("random title_" + new Date().getTime());
      }

      // Action
      String jsonAsString =
          restTemplate.getForObject("http://localhost:" + port + "/job-post", String.class);
      List<JobPost> list =
          new ObjectMapper().readValue(jsonAsString, new TypeReference<List<JobPost>>() {});

      assertThat(list).as("job post list size does not match expected").hasSize(1000);
    }
  }

  @Nested
  class UpdateJobPost {
    @DisplayName("update job post should update all the not null fields")
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
      updateJobPost(
          jobPost.getId(),
          JobPostUpdateRequest.builder()
              .jobTitle("patchedTitle")
              .url("patchedUrl")
              .jobDescription("patchedJobDescription")
              .companyName("patchedCompanyName")
              .location("patchedLocation")
              .status(JobPost.Status.ACCEPTED)
              .build());

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
      assertThat(updatedJobPost.getStatus())
          .as("job post status")
          .isEqualTo(JobPost.Status.ACCEPTED);
    }

    @DisplayName("update job post should not update null fields")
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
      JobPostUpdateRequest request =
          JobPostUpdateRequest.builder().jobTitle("originalTitle").url(null).build();

      updateJobPost(jobPost.getId(), request);

      // Assert
      var updatedJobPost = getJobPost(jobPost.getId());
      assertThat(updatedJobPost.getId()).as("job post id").isEqualTo(jobPost.getId());
      assertThat(updatedJobPost.getUrl()).as("job post url").isEqualTo("originalUrl");
    }

    @DisplayName("last modified date should be updated after update job post")
    @Test
    void Given_JobPostExists_WhenUpdateJobPost_ThenShouldChangeLastModifiedDate() {
      // Arrange
      var jobPost = createAJobPost("Senior Software Developer");

      // Action
      JobPostUpdateRequest request =
          JobPostUpdateRequest.builder().jobTitle("Staff Software Developer").build();
      updateJobPost(jobPost.getId(), request);

      // Assert
      assertThat(getJobPost(jobPost.getId()).getLastModifiedDate())
          .as("last modified date")
          .isAfter(jobPost.getLastModifiedDate());
    }
  }

  @Nested
  class DeleteJobPost {
    @DisplayName("when deleting a not existing id should return http not found status")
    @Test
    void Given_JobPostExists_WhenDeleteJobPost_ThenGetJobPostShouldReturnNotFound() {
      // Arrange
      JobPost jobPost = createAJobPost("job title");

      // Action
      restTemplate.delete("http://localhost:" + port + "/job-post/" + jobPost.getId());

      // Assert
      assertThat(
              restTemplate
                  .getForEntity(
                      "http://localhost:" + port + "/job-post/" + jobPost.getId(), String.class)
                  .getStatusCode())
          .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @DisplayName("get job post should not be able to find the deleted job post")
    @Test
    void Given_JobPostDoesNotExist_WhenDeleteJobPost_ThenShouldReturnNotFound() {
      // Action
      assertThat(
              restTemplate
                  .exchange(
                      "http://localhost:" + port + "/job-post/" + 10,
                      HttpMethod.DELETE,
                      null,
                      String.class)
                  .getStatusCode())
          .as("status code")
          .isEqualTo(HttpStatus.NOT_FOUND);
    }
  }

  // region Utility

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
            "http://localhost:" + port + "/job-post", request, JobPost.class);

    return jobPostResponseEntity.getBody();
  }

  private JobPost createAJobPost(String jobTitle) {
    return createAJobPost(jobTitle, null, null, null, null, null);
  }

  private JobPost getJobPost(long jobPostId) {
    return this.restTemplate.getForObject(
        "http://localhost:" + port + "/job-post/" + jobPostId, JobPost.class);
  }

  private void updateJobPost(Long id, JobPostUpdateRequest jobPostToBePatched) {
    this.restTemplate.patchForObject(
        "http://localhost:" + port + "/job-post/" + id, jobPostToBePatched, JobPost.class);
  }

  // endregion
}
