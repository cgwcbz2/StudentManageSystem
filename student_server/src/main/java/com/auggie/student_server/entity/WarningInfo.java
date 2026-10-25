package com.auggie.student_server.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WarningInfo {
    private Integer sctid;
    private Integer sid;
    private String sname;
    private Integer cid;
    private String cname;
    private Integer tid;
    private String tname;
    private Float grade;
    private String term;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime lastFollowupAt;
}
