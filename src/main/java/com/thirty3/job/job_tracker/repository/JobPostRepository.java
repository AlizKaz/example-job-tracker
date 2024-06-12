package com.thirty3.job.job_tracker.repository;

import com.thirty3.job.job_tracker.model.JobPost;
import org.springframework.data.repository.CrudRepository;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface JobPostRepository extends CrudRepository<JobPost, Long> {}
