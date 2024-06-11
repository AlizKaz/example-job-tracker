package com.thirty3.job.job_tracker.restservice;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class JobDescription {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    public long id;


    public String text;

    public JobDescription() {
    }

    public JobDescription(long id, String text) {
        this.id = id;
        this.text = text;
    }
}
