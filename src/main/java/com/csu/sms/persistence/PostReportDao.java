package com.csu.sms.persistence;

import com.csu.sms.model.post.PostReport;
import com.csu.sms.model.enums.ReportStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface PostReportDao {
    int insertPostReport(@Param("postId") Long postId,@Param("userId") Long userId, @Param("reason") String reason);

    int updateReportStatus(@Param("reportId") Long reportId,@Param("reportStatus") ReportStatus reportStatus,@Param("adminId") Long adminId,@Param("now") LocalDateTime now);

    Long getPostIdByReportId(@Param("reportId") Long reportId);

    PostReport findReportByReportId(@Param("reportId") Long reportId);
}
