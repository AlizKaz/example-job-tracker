package com.thirty3.job.job_tracker.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPost {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  private String jobTitle;

  private String url;

  private String companyName;

  private String location;

  private String jobDescription;

  private JobPost.Status status;

  public enum Status {
    BOOKMARKED,
    APPLYING,
    APPLIED,
    INTERVIEWING,
    NEGOTIATING,
    ACCEPTED,
    ARCHIVED,
    DELETED
  }
}
