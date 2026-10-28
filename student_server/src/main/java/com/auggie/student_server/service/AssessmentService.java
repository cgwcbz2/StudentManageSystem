package com.auggie.student_server.service;

import com.auggie.student_server.entity.AssessmentHistory;
import com.auggie.student_server.entity.AssessmentSubmission;
import com.auggie.student_server.mapper.AssessmentMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AssessmentService {
    private final AssessmentMapper mapper;

    public AssessmentService(AssessmentMapper mapper) {
        this.mapper = mapper;
    }

    public List<AssessmentHistory> findHistory(Integer sctid) {
        requirePassScore(sctid);
        return mapper.findHistory(sctid);
    }

    @Transactional
    public AssessmentHistory record(Integer sctid, AssessmentSubmission submission) {
        if (submission == null || submission.getOperatorTid() == null
                || mapper.countTeacher(submission.getOperatorTid()) == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "录入教师不存在");
        }
        validateGrade(submission.getGrade());
        save(sctid, submission.getGrade(), submission.getOperatorTid(), "manual");
        return mapper.findHistory(sctid).get(0);
    }

    @Transactional
    public boolean recordLegacy(Integer sid, Integer cid, Integer tid, String term, Integer grade) {
        validateGrade(grade);
        Integer sctid = mapper.findSctId(sid, cid, tid, term);
        if (sctid == null) {
            return false;
        }
        save(sctid, grade, null, "legacy_update");
        return true;
    }

    private void save(Integer sctid, Integer grade, Integer operatorTid, String source) {
        Integer passScore = requirePassScore(sctid);
        if (mapper.updateGrade(sctid, grade) != 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "考核任务不存在");
        }
        mapper.insertHistory(sctid, grade, passScore, operatorTid, source);
    }

    private Integer requirePassScore(Integer sctid) {
        Integer passScore = mapper.findPassScore(sctid);
        if (passScore == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "考核任务不存在");
        }
        return passScore;
    }

    private void validateGrade(Integer grade) {
        if (grade == null || grade < 0 || grade > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "考核分数需为 0 至 100 分整数");
        }
    }
}
