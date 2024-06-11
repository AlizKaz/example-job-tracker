package com.thirty3.job.job_tracker.restservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class JobDescriptionController {
    @Autowired
    private JobDescriptionRepository repository;

    @GetMapping("/job-description")
    public Optional<JobDescription> jobDescription(@RequestParam(value = "id") long id) {
        return repository.findById(id);
    }

    @PostMapping("/job-description")
    public JobDescription jobDescription(@RequestBody JobDescription jobDescription) {
        return repository.save(jobDescription);
    }
}
