package com.thirty3.job.job_tracker.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.thirty3.job.job_tracker.controller.dto.CreateJobPost;
import com.thirty3.job.job_tracker.model.JobPost;
import com.thirty3.job.job_tracker.repository.JobPostRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ActiveProfiles("test")
@WebMvcTest(JobPostController.class)
public class JobPostControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private JobPostRepository repository;

  @Test
  public void Given_NoJobPost_When_GetJobPost_Then_ReturnClientError() throws Exception {
    // Arrange
    long id = 1L;

    // Act
    ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/job-post/" + id));

    // Assert
    result.andExpect(status().is4xxClientError());
  }

  @Test
  public void Given_MatchedJobPost_When_GetJobPost_Then_ReturnJobPost() throws Exception {
    // Arrange
    Long id = 1L;
    JobPost jobPost =
        JobPost.builder()
            .id(id)
            .jobDescription("job description")
            .companyName("company name")
            .build();
    Mockito.when(repository.findById(id)).thenReturn(Optional.of(jobPost));

    // Act
    ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/job-post/" + id));

    // Assert
    result
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(id))
        .andExpect(jsonPath("$.jobDescription").value("job description"))
        .andExpect(jsonPath("$.companyName").value("company name"));
  }

  @Test
  public void Given_JobPost_WhenValid_Then_ReturnWithId() throws Exception {
    // Arrange
    CreateJobPost createJobPost = CreateJobPost.builder().jobTitle("<job title>").build();

    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
    ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
    String bodyJson = ow.writeValueAsString(createJobPost);

    Mockito.when(repository.save(Mockito.any()))
        .thenReturn(JobPost.builder().id(1L).jobTitle("<job title>").build());

    // Act
    ResultActions result =
        mockMvc.perform(
            MockMvcRequestBuilders.post("/job-post")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyJson));

    // Assert
    result
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.jobTitle").isString());
  }

  @Test
  public void Given_GetJobPosts_WhenNoJobPosts_ThenReturnClientError() throws Exception {
    // Arrange
    Mockito.when(repository.findAll()).thenReturn(null);

    // Act
    ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/job-post"));

    // Assert
    result.andExpect(status().is4xxClientError());
  }

  @Test
  public void Given_GetJobPosts_WhenListOfJobPostsAvailable_ThenReturnOk() throws Exception {
    // Arrange
    Mockito.when(repository.findAll()).thenReturn(List.of(JobPost.builder().build()));

    // Act
    ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/job-post"));

    // Assert
    result.andExpect(status().isOk());
  }

  @Test
  public void Given_GetJobPosts_WhenListOfJobPostsAvailable_ThenReturnListOfPosts()
      throws Exception {
    // Arrange
    Mockito.when(repository.findAll())
        .thenReturn(List.of(JobPost.builder().build(), JobPost.builder().build()));

    // Act
    ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/job-post"));

    // Assert
    result.andExpect(jsonPath("$.length()").value(2));
  }
}
