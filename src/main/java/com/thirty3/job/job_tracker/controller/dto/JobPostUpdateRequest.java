package com.thirty3.job.job_tracker.controller.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobPostUpdateRequest {
  private String jobTitle;

  private String url;

  private String companyName;

  private String location;

  private String jobDescription;
}
