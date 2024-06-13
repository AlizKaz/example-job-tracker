package com.thirty3.job.job_tracker.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateJobPost {
  private long id;

  @NotBlank(message = "jobTitle can not be blank")
  private String jobTitle;

  private String url;

  private String companyName;

  private String location;

  private String jobDescription;
}
