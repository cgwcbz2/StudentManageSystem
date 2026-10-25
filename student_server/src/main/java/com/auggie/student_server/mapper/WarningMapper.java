package com.auggie.student_server.mapper;

import com.auggie.student_server.entity.WarningFollowup;
import com.auggie.student_server.entity.WarningInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WarningMapper {
    List<WarningInfo> findWarnings(@Param("term") String term,
                                   @Param("cid") Integer cid,
                                   @Param("tid") Integer tid);

    List<WarningFollowup> findFollowups(@Param("sctid") Integer sctid);

    WarningFollowup findFollowupById(@Param("id") Integer id);

    int countStudentCourse(@Param("sctid") Integer sctid);

    int countWarning(@Param("sctid") Integer sctid);

    int countTeacher(@Param("tid") Integer tid);

    int insertFollowup(WarningFollowup followup);
}
