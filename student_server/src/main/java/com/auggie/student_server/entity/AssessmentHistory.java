package com.auggie.student_server.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AssessmentHistory {
    private Long id;
    private Integer sctid;
    private Float grade;
    private Integer passScoreSnapshot;
    private Integer operatorTid;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime recordedAt;
    private String source;

    public String getStatus() {
        return grade < passScoreSnapshot ? "warning" : "passed";
    }
}
