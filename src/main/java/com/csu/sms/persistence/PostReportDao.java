package com.csu.sms.persistence;

import com.csu.sms.model.post.PostReport;
import com.csu.sms.model.enums.ReportStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PostReportDao {
    int insertPostReport(@Param("postId") Long postId,@Param("userId") Long userId, @Param("reason") String reason);

    int updateReportStatus(@Param("reportId") Long reportId,@Param("reportStatus") ReportStatus reportStatus,@Param("adminId") Long adminId,@Param("now") LocalDateTime now);

    Long getPostIdByReportId(@Param("reportId") Long reportId);

    PostReport findReportByReportId(@Param("reportId") Long reportId);

    int countReports(@Param("reportStatus") ReportStatus reportStatus);

    @ResultMap("PostReportResultMap")
    List<PostReport> findReportsByPage(@Param("reportStatus") ReportStatus reportStatus,@Param("offset") int offset,@Param("size") int size);
}
