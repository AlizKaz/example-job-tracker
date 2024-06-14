package com.thirty3.job.job_tracker.controller.dto;

import com.thirty3.job.job_tracker.model.JobPost;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobPostCreateRequest {
  private String jobTitle;

  private String url;

  private String companyName;

  private String location;

  private String jobDescription;

  private JobPost.Status status;
}
