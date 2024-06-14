package com.thirty3.job.job_tracker.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.thirty3.job.job_tracker.serializer.DateToEpochSeconds;
import jakarta.persistence.*;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(AuditingEntityListener.class)
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

  @CreatedDate
  @JsonSerialize(using = DateToEpochSeconds.class)
  private Date createdDate;

  @LastModifiedDate
  @JsonSerialize(using = DateToEpochSeconds.class)
  private Date lastModifiedDate;

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
