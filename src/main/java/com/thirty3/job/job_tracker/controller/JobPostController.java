package com.thirty3.job.job_tracker.controller;

import com.thirty3.job.job_tracker.controller.dto.CreateJobPost;
import com.thirty3.job.job_tracker.controller.dto.JobPostUpdateRequest;
import com.thirty3.job.job_tracker.model.JobPost;
import com.thirty3.job.job_tracker.repository.JobPostRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class JobPostController {
  @Autowired private JobPostRepository repository;

  @GetMapping("/job-post/{id}")
  public JobPost getJobPost(@PathVariable Long id) {
    System.out.println("getting job post with id= " + id);
    Optional<JobPost> jobPostOptional = repository.findById(id);
    if (jobPostOptional.isPresent()) {
      return jobPostOptional.get();
    } else {
      throw new ResourceNotFoundException();
    }
  }

  @GetMapping("/job-post")
  public List<JobPost> getJobPosts() {
    List<JobPost> list = repository.findAll();
    if (list == null) {
      throw new ResourceNotFoundException();
    }
    return list;
  }

  @PostMapping(value = "/job-post", consumes = "application/json")
  public JobPost createJobPost(@Valid @RequestBody CreateJobPost createJobPost) {
    JobPost jobPost = new JobPost();
    jobPost.setJobTitle(createJobPost.getJobTitle());
    jobPost.setCompanyName(createJobPost.getCompanyName());
    jobPost.setUrl(createJobPost.getUrl());
    jobPost.setLocation(createJobPost.getLocation());
    jobPost.setStatus(JobPost.Status.BOOKMARKED);
    return repository.save(jobPost);
  }

  @PatchMapping(value = "/job-post/{id}")
  public JobPost updateJobPost(
      @PathVariable Long id, @Valid @RequestBody JobPostUpdateRequest updateRequest) {
    JobPost loadedJobPost = repository.findById(id).orElseThrow(ResourceNotFoundException::new);

    if (updateRequest.getJobTitle() != null) {
      loadedJobPost.setJobTitle(updateRequest.getJobTitle());
    }
    if (updateRequest.getJobDescription() != null) {
      loadedJobPost.setJobDescription(updateRequest.getJobDescription());
    }
    if (updateRequest.getLocation() != null) {
      loadedJobPost.setLocation(updateRequest.getLocation());
    }
    if (updateRequest.getCompanyName() != null) {
      loadedJobPost.setCompanyName(updateRequest.getCompanyName());
    }
    if (updateRequest.getUrl() != null) {
      loadedJobPost.setUrl(updateRequest.getUrl());
    }
    return repository.save(loadedJobPost);
  }
}
