package com.thirty3.job.job_tracker.controller;

import com.thirty3.job.job_tracker.controller.dto.CreateJobPost;
import com.thirty3.job.job_tracker.model.JobPost;
import com.thirty3.job.job_tracker.repository.JobPostRepository;
import jakarta.validation.Valid;
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

  @PostMapping(value = "/job-post", consumes = "application/json")
  public JobPost createJobPost(@Valid @RequestBody CreateJobPost createJobPost) {
    JobPost jobPost = new JobPost();
    jobPost.setJobTitle(createJobPost.getJobTitle());
    jobPost.setCompanyName(createJobPost.getCompanyName());
    jobPost.setUrl(createJobPost.getUrl());
    jobPost.setLocation(createJobPost.getLocation());
    jobPost.setStatus(JobPost.Status.BOOKMARKED);
    JobPost saved = repository.save(jobPost);
    return saved;
  }
}
