package com.auggie.student_server.mapper;

import com.auggie.student_server.entity.AssessmentHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AssessmentMapper {
    Integer findSctId(@Param("sid") Integer sid, @Param("cid") Integer cid,
                      @Param("tid") Integer tid, @Param("term") String term);

    Integer findPassScore(@Param("sctid") Integer sctid);

    int countTeacher(@Param("tid") Integer tid);

    int updateGrade(@Param("sctid") Integer sctid, @Param("grade") Integer grade);

    int insertHistory(@Param("sctid") Integer sctid, @Param("grade") Integer grade,
                      @Param("passScore") Integer passScore, @Param("operatorTid") Integer operatorTid,
                      @Param("source") String source);

    List<AssessmentHistory> findHistory(@Param("sctid") Integer sctid);

    int countHistory(@Param("sctid") Integer sctid);
}
