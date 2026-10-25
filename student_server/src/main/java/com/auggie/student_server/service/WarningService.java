package com.auggie.student_server.service;

import com.auggie.student_server.entity.WarningFollowup;
import com.auggie.student_server.entity.WarningInfo;
import com.auggie.student_server.mapper.WarningMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class WarningService {
    private final WarningMapper warningMapper;

    public WarningService(WarningMapper warningMapper) {
        this.warningMapper = warningMapper;
    }

    public List<WarningInfo> findWarnings(String term, Integer cid, Integer tid) {
        return warningMapper.findWarnings(term, cid, tid);
    }

    public List<WarningFollowup> findFollowups(Integer sctid) {
        if (warningMapper.countStudentCourse(sctid) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "选课成绩记录不存在");
        }
        return warningMapper.findFollowups(sctid);
    }

    public WarningFollowup addFollowup(Integer sctid, WarningFollowup followup) {
        if (followup == null || followup.getContent() == null
                || followup.getContent().trim().isEmpty()
                || followup.getContent().length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "跟进内容需为 1 至 1000 个字符");
        }
        if (followup.getOperatorTid() == null
                || warningMapper.countTeacher(followup.getOperatorTid()) == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "记录教师不存在");
        }
        if (warningMapper.countStudentCourse(sctid) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "选课成绩记录不存在");
        }
        if (warningMapper.countWarning(sctid) == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前记录不在预警状态");
        }

        followup.setId(null);
        followup.setSctid(sctid);
        followup.setContent(followup.getContent().trim());
        warningMapper.insertFollowup(followup);
        return warningMapper.findFollowupById(followup.getId());
    }
}
