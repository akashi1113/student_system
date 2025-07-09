package com.csu.sms.controller;

import com.csu.sms.service.LogService;
import com.csu.sms.common.PageResult;
import com.csu.sms.common.ApiResponse;
import com.csu.sms.model.OperationLog;
import com.csu.sms.vo.OperationLogReportVO;
import com.csu.sms.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;


@RestController
@RequestMapping("/api/audit/report")
public class AuditReportController {

    @Autowired
    private LogService logService;

    @GetMapping("/operation")
    public ApiResponse<PageResult<OperationLogReportVO>> getOperationAuditReport(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String module,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
//        // 权限检查：只有管理员可以查看审计报告
//        if (!UserContext.isAdmin()) {
//            return ApiResponse.error("权限不足，只有管理员可以查看审计报告", "PERMISSION_DENIED");
//        }
        
        try {
            PageResult<OperationLogReportVO> result = logService.getOperationAuditReport(
                userId, username, startTime, endTime, operation, module, page, size);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("获取审计报告失败: " + e.getMessage());
        }
    }

    /**
     * 分组明细接口：根据分组条件查询原始操作日志明细
     */
    @GetMapping("/operation/details")
    public ApiResponse<PageResult<OperationLog>> getOperationLogDetails(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // 权限检查：只有管理员可以查看审计报告
//        if (!UserContext.isAdmin()) {
//            return ApiResponse.error("权限不足，只有管理员可以查看审计报告", "PERMISSION_DENIED");
//        }
        
        try {
            PageResult<OperationLog> result = logService.getOperationLogDetails(
                username, module, operation, startTime, endTime, page, size);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("获取操作日志详情失败: " + e.getMessage());
        }
    }

    @GetMapping("/operation/export")
    public void exportOperationLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            jakarta.servlet.http.HttpServletResponse response
    ) throws java.io.IOException {
//        // 权限检查：只有管理员可以导出审计报告
//        if (!UserContext.isAdmin()) {
//            response.setStatus(403);
//            response.setContentType("application/json");
//            response.getWriter().write("{\"code\":403,\"message\":\"权限不足，只有管理员可以导出审计报告\",\"errorCode\":\"PERMISSION_DENIED\"}");
//            return;
//        }
        
        try {
            java.util.List<com.csu.sms.model.OperationLog> logs = logService.getOperationLogDetails(
                    username, module, operation, startTime, endTime, 1, Integer.MAX_VALUE
            ).getList();

            java.util.List<com.csu.sms.vo.OperationLogExportVO> exportList = logs.stream().map(log -> {
                com.csu.sms.vo.OperationLogExportVO vo = new com.csu.sms.vo.OperationLogExportVO();
                vo.setId(log.getId());
                vo.setUsername(log.getUsername());
                vo.setModule(log.getModule());
                vo.setOperation(log.getOperation());
                vo.setDescription(log.getDescription());
                vo.setStatus(log.getStatus());
                vo.setIpAddress(log.getIpAddress());
                vo.setCreateTime(log.getCreateTime() != null ? log.getCreateTime().toString() : "");
                return vo;
            }).collect(java.util.stream.Collectors.toList());

            // 强制使用xlsx格式
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=operation_logs.xlsx");

            cn.hutool.poi.excel.ExcelWriter writer = cn.hutool.poi.excel.ExcelUtil.getWriter(true); // true 表示xlsx
            writer.write(exportList, true);
            writer.autoSizeColumnAll(); // 自动调整所有列宽
            writer.setColumnWidth(2, 20); // module列
            writer.setColumnWidth(3, 20); // operation列
            writer.setColumnWidth(4, 40);
            writer.setColumnWidth(5, 15); // status列
            writer.setColumnWidth(6, 15); // ipAddress列
            writer.setColumnWidth(7, 20); // createTime列

            writer.flush(response.getOutputStream());
            writer.close();
        } catch (Exception e) {
            response.setStatus(500);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":500,\"message\":\"导出失败: " + e.getMessage() + "\"}");
        }
    }
} 