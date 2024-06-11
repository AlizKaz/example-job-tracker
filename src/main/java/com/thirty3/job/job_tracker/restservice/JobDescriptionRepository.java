package com.thirty3.job.job_tracker.restservice;

import org.springframework.data.repository.CrudRepository;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface JobDescriptionRepository extends CrudRepository<JobDescription, Long> {

}