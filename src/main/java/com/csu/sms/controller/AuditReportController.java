package com.csu.sms.controller;

import com.csu.sms.service.LogService;
import com.csu.sms.common.PageResult;
import com.csu.sms.model.OperationLog;
import com.csu.sms.vo.OperationLogReportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;


@RestController
@RequestMapping("/api/audit/report")
public class AuditReportController {

    @Autowired
    private LogService logService;

    @GetMapping("/operation")
    public PageResult<OperationLogReportVO> getOperationAuditReport(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String module,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return logService.getOperationAuditReport(userId, username, startTime, endTime, operation, module, page, size);
    }

    /**
     * 分组明细接口：根据分组条件查询原始操作日志明细
     */
    @GetMapping("/operation/details")
    public PageResult<OperationLog> getOperationLogDetails(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return logService.getOperationLogDetails(username, module, operation, startTime, endTime, page, size);
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
        writer.setColumnWidth(4, 40); // description列
        writer.flush(response.getOutputStream(), true);
        writer.close();
    }
} 