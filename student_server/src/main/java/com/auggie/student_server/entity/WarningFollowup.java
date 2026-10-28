package com.auggie.student_server.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WarningFollowup {
    private Integer id;
    private Integer sctid;
    private String content;
    private Integer operatorTid;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
}
