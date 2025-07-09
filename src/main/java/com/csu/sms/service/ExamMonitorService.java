package com.csu.sms.service;

import com.csu.sms.model.ExamMonitor;
import com.csu.sms.model.ExamMonitorSummary;
import com.csu.sms.model.MonitorAnalysisResult;
import com.csu.sms.persistence.ExamMonitorMapper;
import com.csu.sms.persistence.ExamMonitorSummaryMapper;
import com.csu.sms.util.BaiduFaceApiUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExamMonitorService {

    @Autowired
    private BaiduFaceApiUtil baiduFaceApiUtil;

    @Autowired
    private ExamMonitorMapper examMonitorMapper;

    @Autowired
    private ExamMonitorSummaryMapper examMonitorSummaryMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(ExamMonitorService.class);

    /**
     * 处理监考图片 - 增强版本，先检测人脸角度，再进行身份验证
     */
    public void processMonitorImage(String imagePath, Long examId, Long userId, Long timestamp) {
        try {
            // 读取图片并转换为Base64
            String imageBase64 = convertImageToBase64(imagePath);

            // 首先进行人脸检测，获取人脸数量和角度信息
            JsonNode detectResult = baiduFaceApiUtil.faceDetect(imageBase64);

            // 分析人脸检测结果
            MonitorAnalysisResult analysisResult = analyzeDetectionResult(detectResult);

            // 如果人脸检测正常（单人脸），则进行身份验证
            if (!analysisResult.isAbnormal()) {
                JsonNode searchResult = baiduFaceApiUtil.faceSearch(imageBase64);
                analysisResult = enhanceWithIdentityVerification(searchResult, userId, analysisResult);
            }

            // 保存监考记录
            saveMonitorRecord(examId, userId, imagePath, timestamp, analysisResult);

            // 更新监考统计
            updateMonitorSummary(examId, userId, analysisResult);

            // 如果发现异常，发送预警
            if (analysisResult.hasAbnormal()) {
                sendAlert(examId, userId, analysisResult);
            }

        } catch (Exception e) {
            logger.error("处理监考图片失败: examId={}, userId={}", examId, userId, e);
        }
    }

    /**
     * 新增：分析人脸检测结果
     */
    private MonitorAnalysisResult analyzeDetectionResult(JsonNode detectResult) {
        MonitorAnalysisResult result = new MonitorAnalysisResult();

        logger.info("人脸检测API响应: {}", detectResult.toString());

        int errorCode = detectResult.get("error_code").asInt();
        if (errorCode != 0) {
            String errorMsg = detectResult.has("error_msg") ?
                    detectResult.get("error_msg").asText() : "未知错误";

            logger.error("人脸检测API调用失败 - 错误代码: {}, 错误信息: {}", errorCode, errorMsg);

            result.setAbnormalType("API_ERROR");
            result.setAbnormal(true);
            result.setMessage("人脸检测API调用失败: " + errorMsg + " (错误代码: " + errorCode + ")");
            return result;
        }

        if (!detectResult.has("result")) {
            logger.error("人脸检测API响应缺少result节点");
            result.setAbnormalType("API_ERROR");
            result.setAbnormal(true);
            result.setMessage("人脸检测API响应格式错误");
            return result;
        }

        JsonNode resultNode = detectResult.get("result");

        if (!resultNode.has("face_list")) {
            logger.error("人脸检测API响应缺少face_list节点");
            result.setAbnormalType("API_ERROR");
            result.setAbnormal(true);
            result.setMessage("人脸检测API响应格式错误");
            return result;
        }

        JsonNode faceList = resultNode.get("face_list");
        int faceCount = faceList.size();

        logger.info("检测到人脸数量: {}", faceCount);

        if (faceCount == 0) {
            result.setAbnormalType("NO_FACE");
            result.setAbnormal(true);
            result.setMessage("未检测到人脸");
        } else if (faceCount > 1) {
            result.setAbnormalType("MULTIPLE_FACES");
            result.setAbnormal(true);
            result.setMessage("检测到多张人脸");
        } else {
            // 单张人脸，分析角度信息
            JsonNode face = faceList.get(0);
            result = analyzeFaceAngle(face, result);
        }

        return result;
    }

    /**
     * 新增：分析人脸角度
     */
    private MonitorAnalysisResult analyzeFaceAngle(JsonNode face, MonitorAnalysisResult result) {
        try {
            if (!face.has("angle")) {
                logger.warn("人脸数据缺少angle节点");
                result.setMessage("人脸角度信息缺失，但检测到单张人脸");
                return result;
            }

            JsonNode angle = face.get("angle");
            double yaw = angle.get("yaw").asDouble();
            double pitch = angle.get("pitch").asDouble();
            double roll = angle.get("roll").asDouble();

            result.setYawAngle(yaw);
            result.setPitchAngle(pitch);
            result.setRollAngle(roll);

            logger.info("人脸角度 - yaw: {}, pitch: {}, roll: {}", yaw, pitch, roll);

            // 检查面部朝向
            if (Math.abs(yaw) > 30 || Math.abs(pitch) > 20) {
                result.setAbnormalType("LOOKING_AWAY");
                result.setAbnormal(true);
                result.setMessage("偏离屏幕，偏角: yaw=" + yaw + ", pitch=" + pitch);
            } else {
                result.setMessage("检测到正常人脸，准备进行身份验证");
            }

            // 可选：添加质量检查
            if (face.has("quality")) {
                JsonNode quality = face.get("quality");
                if (quality.has("completeness")) {
                    double completeness = quality.get("completeness").asDouble();
                    logger.info("人脸完整度: {}", completeness);
                    if (completeness < 0.6) {
                        result.setAbnormalType("LOW_QUALITY");
                        result.setAbnormal(true);
                        result.setMessage("人脸质量过低，完整度: " + completeness);
                    }
                }
            }

        } catch (Exception e) {
            logger.error("分析人脸角度时发生错误", e);
            result.setMessage("人脸角度分析失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 新增：使用身份验证增强检测结果
     */
    private MonitorAnalysisResult enhanceWithIdentityVerification(JsonNode searchResult, Long userId, MonitorAnalysisResult result) {
        logger.info("开始身份验证增强");

        // 如果之前的检测已经发现异常，直接返回
        if (result.isAbnormal()) {
            logger.info("跳过身份验证，因为已发现异常: {}", result.getAbnormalType());
            return result;
        }

        // 使用原有的身份验证逻辑
        MonitorAnalysisResult identityResult = analyzeMonitorResult(searchResult, userId);

        // 合并检测结果和身份验证结果
        if (identityResult.isAbnormal()) {
            // 如果身份验证异常，使用身份验证的结果
            result.setAbnormal(true);
            result.setAbnormalType(identityResult.getAbnormalType());
            result.setMessage(identityResult.getMessage());
        } else {
            // 身份验证成功，更新消息
            result.setMessage("监考正常，身份验证成功");
        }

        // 设置身份验证相关信息
        result.setScore(identityResult.getScore());
        result.setDetectedUserId(identityResult.getDetectedUserId());

        return result;
    }

    /**
     * 原有的分析监考结果方法保持不变
     */
    private MonitorAnalysisResult analyzeMonitorResult(JsonNode searchResult, Long userId) {
        MonitorAnalysisResult result = new MonitorAnalysisResult();

        logger.info("人脸识别API响应: {}", searchResult.toString());

        int errorCode = searchResult.get("error_code").asInt();
        if (errorCode != 0) {
            String errorMsg = searchResult.has("error_msg") ?
                    searchResult.get("error_msg").asText() : "未知错误";

            logger.error("人脸识别API调用失败 - 错误代码: {}, 错误信息: {}", errorCode, errorMsg);

            result.setAbnormalType("API_ERROR");
            result.setAbnormal(true);
            result.setMessage("人脸识别API调用失败: " + errorMsg + " (错误代码: " + errorCode + ")");
            return result;
        }

        if (!searchResult.has("result")) {
            logger.error("API响应缺少result节点");
            result.setAbnormalType("API_ERROR");
            result.setAbnormal(true);
            result.setMessage("API响应格式错误");
            return result;
        }

        JsonNode resultNode = searchResult.get("result");

        // 修改：检查user_list而不是face_list
        if (!resultNode.has("user_list")) {
            logger.error("API响应缺少user_list节点");
            result.setAbnormalType("API_ERROR");
            result.setAbnormal(true);
            result.setMessage("API响应格式错误");
            return result;
        }

        JsonNode userList = resultNode.get("user_list");
        logger.info("检测到用户数量: {}", userList.size());

        // 处理人脸搜索结果
        return processSearchResult(userList, userId, result);
    }

    /**
     * 原有的处理搜索结果方法保持不变
     */
    private MonitorAnalysisResult processSearchResult(JsonNode userList, Long userId, MonitorAnalysisResult result) {
        try {
            if (userList.size() == 0) {
                // 未找到匹配的用户（可能是未检测到人脸或无匹配用户）
                result.setAbnormalType("NO_FACE");
                result.setAbnormal(true);
                result.setMessage("未检测到人脸或无匹配用户");
                return result;
            }

            // 获取最佳匹配用户
            JsonNode topUser = userList.get(0);
            String detectedUserId = topUser.get("user_id").asText();
            double score = topUser.get("score").asDouble();

            result.setScore(score);
            result.setDetectedUserId(detectedUserId);

            logger.info("身份验证 - 检测到用户ID: {}, 匹配度: {}, 期望用户ID: {}",
                    detectedUserId, score, userId);

            // 检查身份是否匹配
            if (!detectedUserId.equals(userId.toString())) {
                result.setAbnormalType("UNKNOWN_PERSON");
                result.setAbnormal(true);
                result.setMessage("身份不匹配，检测到用户: " + detectedUserId + "，期望用户: " + userId);
                return result;
            }

            // 检查匹配度是否足够高
            if (score < 80) {
                result.setAbnormalType("UNKNOWN_PERSON");
                result.setAbnormal(true);
                result.setMessage("身份验证失败，匹配度过低: " + score);
                return result;
            }

            // 身份验证成功
            result.setMessage("监考正常，身份验证成功");

        } catch (Exception e) {
            logger.error("处理人脸搜索结果时发生错误", e);
            result.setAbnormalType("API_ERROR");
            result.setAbnormal(true);
            result.setMessage("人脸搜索结果处理失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 新增：仅使用人脸检测的处理方法（不进行身份验证）
     */
    public void processMonitorImageDetectionOnly(String imagePath, Long examId, Long userId, Long timestamp) {
        try {
            // 读取图片并转换为Base64
            String imageBase64 = convertImageToBase64(imagePath);

            // 只进行人脸检测
            JsonNode detectResult = baiduFaceApiUtil.faceDetect(imageBase64);

            // 分析人脸检测结果
            MonitorAnalysisResult analysisResult = analyzeDetectionResult(detectResult);

            // 保存监考记录
            saveMonitorRecord(examId, userId, imagePath, timestamp, analysisResult);

            // 更新监考统计
            updateMonitorSummary(examId, userId, analysisResult);

            // 如果发现异常，发送预警
            if (analysisResult.hasAbnormal()) {
                sendAlert(examId, userId, analysisResult);
            }

        } catch (Exception e) {
            logger.error("处理监考图片失败(仅检测): examId={}, userId={}", examId, userId, e);
        }
    }

    /**
     * 新增：获取人脸检测统计信息
     */
    public Map<String, Object> getFaceDetectionStats(Long examId, Long userId) {
        Map<String, Object> stats = new HashMap<>();

        List<ExamMonitor> records = getMonitorRecords(examId, userId);

        int totalCount = records.size();
        int noFaceCount = 0;
        int multipleFacesCount = 0;
        int lookingAwayCount = 0;
        int lowQualityCount = 0;
        int normalCount = 0;

        double totalYaw = 0, totalPitch = 0, totalRoll = 0;
        int angleCount = 0;

        for (ExamMonitor record : records) {
            if ("NO_FACE".equals(record.getAbnormalType())) {
                noFaceCount++;
            } else if ("MULTIPLE_FACES".equals(record.getAbnormalType())) {
                multipleFacesCount++;
            } else if ("LOOKING_AWAY".equals(record.getAbnormalType())) {
                lookingAwayCount++;
            } else if ("LOW_QUALITY".equals(record.getAbnormalType())) {
                lowQualityCount++;
            } else if (record.getStatus() == 1) {
                normalCount++;
            }

            // 统计角度信息
            if (record.getYawAngle() != null) {
                totalYaw += record.getYawAngle().doubleValue();
                angleCount++;
            }
            if (record.getPitchAngle() != null) {
                totalPitch += record.getPitchAngle().doubleValue();
            }
            if (record.getRollAngle() != null) {
                totalRoll += record.getRollAngle().doubleValue();
            }
        }

        stats.put("totalCount", totalCount);
        stats.put("noFaceCount", noFaceCount);
        stats.put("multipleFacesCount", multipleFacesCount);
        stats.put("lookingAwayCount", lookingAwayCount);
        stats.put("lowQualityCount", lowQualityCount);
        stats.put("normalCount", normalCount);

        if (angleCount > 0) {
            stats.put("avgYawAngle", totalYaw / angleCount);
            stats.put("avgPitchAngle", totalPitch / angleCount);
            stats.put("avgRollAngle", totalRoll / angleCount);
        }

        return stats;
    }

    /**
     * 新增：获取角度异常的记录
     */
    public List<ExamMonitor> getAngleAbnormalRecords(Long examId, Long userId, double yawThreshold, double pitchThreshold) {
        List<ExamMonitor> allRecords = getMonitorRecords(examId, userId);

        return allRecords.stream()
                .filter(record -> {
                    if (record.getYawAngle() == null || record.getPitchAngle() == null) {
                        return false;
                    }
                    double yaw = Math.abs(record.getYawAngle().doubleValue());
                    double pitch = Math.abs(record.getPitchAngle().doubleValue());
                    return yaw > yawThreshold || pitch > pitchThreshold;
                })
                .collect(Collectors.toList());
    }

    // 以下是原有的方法，保持不变

    /**
     * 保存监考记录
     */
    private void saveMonitorRecord(Long examId, Long userId, String imagePath,
                                   Long timestamp, MonitorAnalysisResult analysisResult) {
        ExamMonitor monitor = new ExamMonitor();
        monitor.setExamId(examId);
        monitor.setUserId(userId);
        monitor.setImagePath(imagePath);
        monitor.setTimestamp(timestamp);
        monitor.setAbnormalType(analysisResult.getAbnormalType());
        monitor.setAbnormalCount(analysisResult.isAbnormal() ? 1 : 0);
        monitor.setStatus(analysisResult.isAbnormal() ? 0 : 1);

        // 设置人脸识别相关信息
        if (analysisResult.getScore() > 0) {
            monitor.setFaceScore(BigDecimal.valueOf(analysisResult.getScore()));
        }
        if (analysisResult.getYawAngle() != 0) {
            monitor.setYawAngle(BigDecimal.valueOf(analysisResult.getYawAngle()));
        }
        if (analysisResult.getPitchAngle() != 0) {
            monitor.setPitchAngle(BigDecimal.valueOf(analysisResult.getPitchAngle()));
        }
        if (analysisResult.getRollAngle() != 0) {
            monitor.setRollAngle(BigDecimal.valueOf(analysisResult.getRollAngle()));
        }

        try {
            monitor.setMonitorResult(objectMapper.writeValueAsString(analysisResult));
        } catch (Exception e) {
            logger.error("序列化监考结果失败", e);
        }

        examMonitorMapper.insert(monitor);
    }

    /**
     * 更新监考统计
     */
    private void updateMonitorSummary(Long examId, Long userId, MonitorAnalysisResult analysisResult) {
        try {
            ExamMonitorSummary summary = examMonitorSummaryMapper.selectByExamIdAndUserId(examId, userId);
            if (summary == null) {
                summary = new ExamMonitorSummary();
                summary.setExamId(examId);
                summary.setUserId(userId);
                summary.setTotalCount(0);
                summary.setAbnormalCount(0);
                summary.setNoFaceCount(0);
                summary.setMultipleFacesCount(0);
                summary.setLookingAwayCount(0);
                summary.setUnknownPersonCount(0);
                summary.setRiskLevel("LOW");
            }

            // 更新总数
            summary.setTotalCount(summary.getTotalCount() + 1);

            // 更新异常统计
            if (analysisResult.isAbnormal()) {
                summary.setAbnormalCount(summary.getAbnormalCount() + 1);

                switch (analysisResult.getAbnormalType()) {
                    case "NO_FACE":
                        summary.setNoFaceCount(summary.getNoFaceCount() + 1);
                        break;
                    case "MULTIPLE_FACES":
                        summary.setMultipleFacesCount(summary.getMultipleFacesCount() + 1);
                        break;
                    case "LOOKING_AWAY":
                        summary.setLookingAwayCount(summary.getLookingAwayCount() + 1);
                        break;
                    case "UNKNOWN_PERSON":
                        summary.setUnknownPersonCount(summary.getUnknownPersonCount() + 1);
                        break;
                    case "LOW_QUALITY":
                        // 可以添加新的字段来统计低质量图片
                        break;
                }
            }

            // 计算风险等级
            summary.setRiskLevel(calculateRiskLevel(summary));

            // 保存或更新统计数据
            examMonitorSummaryMapper.upsert(summary);

        } catch (Exception e) {
            logger.error("更新监考统计失败: examId={}, userId={}", examId, userId, e);
        }
    }

    // 其他原有方法保持不变...

    /**
     * 计算风险等级
     */
    private String calculateRiskLevel(ExamMonitorSummary summary) {
        if (summary.getTotalCount() == 0) {
            return "LOW";
        }

        double abnormalRate = (double) summary.getAbnormalCount() / summary.getTotalCount();

        if (abnormalRate >= 0.3 || summary.getUnknownPersonCount() > 0) {
            return "HIGH";
        } else if (abnormalRate >= 0.1) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * 获取监考记录
     */
    public List<ExamMonitor> getMonitorRecords(Long examId, Long userId) {
        if (userId != null) {
            return examMonitorMapper.selectByExamIdAndUserId(examId, userId);
        } else {
            return examMonitorMapper.selectByExamId(examId);
        }
    }

    /**
     * 获取异常监考记录
     */
    public List<ExamMonitor> getAbnormalMonitorRecords(Long examId, Long userId) {
        return examMonitorMapper.selectAbnormalRecords(examId, userId);
    }

    /**
     * 获取监考统计
     */
    public ExamMonitorSummary getMonitorSummary(Long examId, Long userId) {
        return examMonitorSummaryMapper.selectByExamIdAndUserId(examId, userId);
    }

    /**
     * 获取考试监考统计列表
     */
    public List<ExamMonitorSummary> getExamMonitorSummaries(Long examId) {
        return examMonitorSummaryMapper.selectByExamId(examId);
    }

    /**
     * 获取监考状态
     */
    public Map<String, Object> getMonitorStatus(Long examId, Long userId) {
        Map<String, Object> status = new HashMap<>();

        // 获取总记录数
        int totalCount = examMonitorMapper.countByExamIdAndUserId(examId, userId);
        int abnormalCount = examMonitorMapper.countAbnormalByExamIdAndUserId(examId, userId);

        status.put("totalCount", totalCount);
        status.put("abnormalCount", abnormalCount);
        status.put("normalCount", totalCount - abnormalCount);

        if (totalCount > 0) {
            status.put("abnormalRate", (double) abnormalCount / totalCount);
        } else {
            status.put("abnormalRate", 0.0);
        }

        // 获取最近的监考记录
        List<ExamMonitor> recentRecords = examMonitorMapper.selectByExamIdAndUserId(examId, userId);
        if (!recentRecords.isEmpty()) {
            ExamMonitor latestRecord = recentRecords.get(0);
            status.put("latestStatus", latestRecord.getStatus() == 1 ? "normal" : "abnormal");
            status.put("latestAbnormalType", latestRecord.getAbnormalType());
            status.put("latestTimestamp", latestRecord.getTimestamp());
        }

        return status;
    }

    /**
     * 获取时间范围内的监考记录
     */
    public List<ExamMonitor> getMonitorRecordsByTimeRange(Long examId, Long userId, Long startTime, Long endTime) {
        return examMonitorMapper.selectByTimeRange(examId, userId, startTime, endTime);
    }

    /**
     * 删除监考记录
     */
    public boolean deleteMonitorRecord(Long id) {
        try {
            int result = examMonitorMapper.deleteById(id);
            return result > 0;
        } catch (Exception e) {
            logger.error("删除监考记录失败: id={}", id, e);
            return false;
        }
    }

    /**
     * 获取异常统计
     */
    public Map<String, Object> getAbnormalStats(Long examId, Long userId) {
        Map<String, Object> stats = new HashMap<>();

        List<ExamMonitor> abnormalRecords = examMonitorMapper.selectAbnormalRecords(examId, userId);

        // 按异常类型分组统计
        Map<String, List<ExamMonitor>> groupedByType = abnormalRecords.stream()
                .collect(Collectors.groupingBy(record ->
                        record.getAbnormalType() != null ? record.getAbnormalType() : "UNKNOWN"));

        Map<String, Integer> typeCount = new HashMap<>();
        for (Map.Entry<String, List<ExamMonitor>> entry : groupedByType.entrySet()) {
            typeCount.put(entry.getKey(), entry.getValue().size());
        }

        stats.put("abnormalTypeCount", typeCount);
        stats.put("totalAbnormalCount", abnormalRecords.size());

        // 按时间分布统计（按小时）
        Map<String, Integer> timeDistribution = new HashMap<>();
        for (ExamMonitor record : abnormalRecords) {
            String hour = new Date(record.getTimestamp()).toString().substring(11, 13);
            timeDistribution.put(hour, timeDistribution.getOrDefault(hour, 0) + 1);
        }
        stats.put("timeDistribution", timeDistribution);

        return stats;
    }

    /**
     * 发送预警
     */
    private void sendAlert(Long examId, Long userId, MonitorAnalysisResult result) {
        // 这里可以实现WebSocket实时推送给监考老师
        // 或者发送邮件/短信通知
        logger.warn("监考异常预警: examId={}, userId={}, abnormalType={}, message={}",
                examId, userId, result.getAbnormalType(), result.getMessage());

        // TODO: 实现实时推送
        // webSocketService.sendAlert(examId, userId, result);

        // TODO: 实现邮件通知
        // emailService.sendAlertEmail(examId, userId, result);
    }

    /**
     * 图片转Base64
     */
    private String convertImageToBase64(String imagePath) throws IOException {
        byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    /**
     * 批量处理监考图片
     */
    public void batchProcessMonitorImages(List<String> imagePaths, Long examId, Long userId) {
        for (String imagePath : imagePaths) {
            try {
                long timestamp = System.currentTimeMillis();
                processMonitorImage(imagePath, examId, userId, timestamp);
                // 避免频繁调用API
                Thread.sleep(100);
            } catch (Exception e) {
                logger.error("批量处理监考图片失败: imagePath={}", imagePath, e);
            }
        }
    }

    /**
     * 清理过期的监考记录
     */
    public void cleanExpiredMonitorRecords(Long examId, Long expiredTimestamp) {
        try {
            List<ExamMonitor> expiredRecords = examMonitorMapper.selectByExamId(examId).stream()
                    .filter(record -> record.getTimestamp() < expiredTimestamp)
                    .collect(Collectors.toList());

            for (ExamMonitor record : expiredRecords) {
                // 删除图片文件
                try {
                    Files.deleteIfExists(Paths.get(record.getImagePath()));
                } catch (Exception e) {
                    logger.error("删除过期监考图片失败: {}", record.getImagePath(), e);
                }

                // 删除数据库记录
                examMonitorMapper.deleteById(record.getId());
            }

            logger.info("清理过期监考记录完成: examId={}, cleanedCount={}", examId, expiredRecords.size());
        } catch (Exception e) {
            logger.error("清理过期监考记录失败: examId={}", examId, e);
        }
    }
}