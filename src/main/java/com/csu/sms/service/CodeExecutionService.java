package com.csu.sms.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import java.util.*;

@Service
@Slf4j
public class CodeExecutionService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // 从配置文件读取API配置
    @Value("${judge0.api.url}")
    private String apiUrl;

    @Value("${judge0.api.key}")
    private String apiKey;

    @Value("${judge0.api.host}")
    private String apiHost;

    private static final int MAX_SUBMIT_RETRIES = 3; // 最大重试次数
    private static final long SUBMIT_RETRY_DELAY_MS = 1000; // 重试间隔

    private static final long TIMEOUT_SECONDS = 30; // API调用超时时间
    private static final int MAX_POLL_ATTEMPTS = 15; // 增加轮询次数
    private static final long POLL_INTERVAL_MS = 2000; // 增加轮询间隔

    public CodeExecutionService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public CodeExecutionResult executeCode(String code, String language, String className, String input, String expectedOutput) {
        try {
            log.info("开始执行代码，语言: {}", language);

            // 对Java代码进行预处理
            if ("java".equalsIgnoreCase(language)) {
                code = preprocessJavaCode(code);
            }


            // 1. 提交代码执行请求 - 带重试机制
            String submissionId = submitCodeWithRetry(code, language, input);
            if (submissionId == null) {
                return createErrorResult("提交代码失败，已重试" + MAX_SUBMIT_RETRIES + "次");
            }

            log.info("代码提交成功，submission ID: {}", submissionId);

            // 2. 轮询获取执行结果
            ApiExecutionResult apiResult = pollForResult(submissionId);
            if (apiResult == null) {
                return createErrorResult("获取执行结果超时");
            }

            // 3. 转换为本地结果格式
            CodeExecutionResult result = convertApiResult(apiResult);

            // 4. 检查答案
            if (expectedOutput != null && !expectedOutput.trim().isEmpty()) {
                boolean isCorrect = checkAnswer(result.getOutput(), expectedOutput);
                result.setCorrect(isCorrect);
            }

            return result;

        } catch (Exception e) {
            log.error("API代码执行异常", e);
            return createErrorResult("执行异常: " + e.getMessage());
        }
    }

    private String submitCodeWithRetry(String code, String language, String input) {
        for (int attempt = 1; attempt <= MAX_SUBMIT_RETRIES; attempt++) {
            try {
                log.info("尝试提交代码，第{}次，最多{}次", attempt, MAX_SUBMIT_RETRIES);

                String submissionId = submitCode(code, language, input);
                if (submissionId != null) {
                    return submissionId;
                }

                log.warn("第{}次提交失败，准备重试", attempt);

                // 最后一次失败不需要等待
                if (attempt < MAX_SUBMIT_RETRIES) {
                    Thread.sleep(SUBMIT_RETRY_DELAY_MS);
                }

            } catch (Exception e) {
                log.error("第{}次提交代码异常", attempt, e);

                // 最后一次失败不需要等待
                if (attempt < MAX_SUBMIT_RETRIES) {
                    try {
                        Thread.sleep(SUBMIT_RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        return null;
    }

    // 兼容旧版本的方法
    public CodeExecutionResult executeCode(String code, String className, String input, String expectedOutput) {
        return executeCode(code, "java", className, input, expectedOutput);
    }

    private String submitCode(String code, String language, String input) {
        try {
            // 修改URL：在URL中明确指定base64_encoded参数
            String submitUrl = apiUrl + "/submissions?base64_encoded=true";

            // 构建请求体 - 使用Base64编码
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("source_code", Base64.getEncoder().encodeToString(code.getBytes("UTF-8")));
            requestBody.put("language_id", getLanguageId(language));
            // 不要在请求体中再次设置base64_encoded

            if (input != null && !input.isEmpty()) {
                requestBody.put("stdin", Base64.getEncoder().encodeToString(input.getBytes("UTF-8")));
            }

            // 设置请求头
            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("提交代码到: {}", submitUrl);
            log.debug("请求体: {}", requestBody);

            // 发送请求
            ResponseEntity<Map> response = restTemplate.postForEntity(submitUrl, entity, Map.class);

            log.info("API响应状态: {}", response.getStatusCode());
            log.debug("API响应体: {}", response.getBody());

            if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null) {
                return (String) response.getBody().get("token");
            }

            return null;

        } catch (Exception e) {
            log.error("提交代码失败", e);
            return null;
        }
    }

    private ApiExecutionResult pollForResult(String submissionId) {
        String getUrl = apiUrl + "/submissions/" + submissionId + "?base64_encoded=true&fields=*";
        HttpHeaders headers = createHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        for (int attempt = 0; attempt < MAX_POLL_ATTEMPTS; attempt++) {
            try {
                log.debug("轮询结果，尝试次数: {}, URL: {}", attempt + 1, getUrl);

                ResponseEntity<ApiExecutionResult> response = restTemplate.exchange(
                        getUrl, HttpMethod.GET, entity, ApiExecutionResult.class);

                if (response.getStatusCode() == HttpStatus.OK) {
                    ApiExecutionResult result = response.getBody();
                    log.debug("轮询响应: {}", result);

                    // 检查是否执行完成
                    if (result != null && result.getStatus() != null) {
                        Integer statusId = result.getStatus().getId();
                        log.debug("当前状态ID: {}, 描述: {}", statusId, result.getStatus().getDescription());

                        if (statusId != null && statusId > 2) { // 状态ID > 2 表示执行完成
                            log.info("执行完成，状态: {}", result.getStatus().getDescription());
                            return result;
                        }
                    }
                }

                // 等待后重试
                Thread.sleep(POLL_INTERVAL_MS);

            } catch (Exception e) {
                log.warn("轮询执行结果失败，尝试次数: {}", attempt + 1, e);

                try {
                    Thread.sleep(POLL_INTERVAL_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        return null;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-RapidAPI-Key", apiKey);
        headers.set("X-RapidAPI-Host", apiHost);
        return headers;
    }

    private int getLanguageId(String language) {
        switch (language.toLowerCase()) {
            case "java":
                return 62; // Java (OpenJDK 13.0.1)
            case "python":
            case "python3":
                return 71; // Python (3.8.1)
            case "cpp":
            case "c++":
                return 54; // C++ (GCC 9.2.0)
            case "c":
                return 50; // C (GCC 9.2.0)
            case "javascript":
            case "js":
                return 63; // JavaScript (Node.js 12.14.0)
            case "go":
                return 60; // Go (1.13.5)
            case "rust":
                return 73; // Rust (1.40.0)
            case "kotlin":
                return 78; // Kotlin (1.3.70)
            case "swift":
                return 83; // Swift (5.2.3)
            default:
                throw new IllegalArgumentException("不支持的编程语言: " + language);
        }
    }

    private CodeExecutionResult convertApiResult(ApiExecutionResult apiResult) {
        CodeExecutionResult result = new CodeExecutionResult();

        Status status = apiResult.getStatus();
        if (status == null) {
            result.setStatus("UNKNOWN_ERROR");
            result.setOutput("未知错误");
            return result;
        }

        log.debug("转换API结果，状态ID: {}, stdout: {}, stderr: {}, compile_output: {}",
                status.getId(), apiResult.getStdout(), apiResult.getStderr(), apiResult.getCompile_output());

        // 转换状态 - 对所有输出都进行Base64解码
        switch (status.getId()) {
            case 3: // Accepted
                result.setStatus("SUCCESS");
                result.setOutput(decodeBase64OrDefault(apiResult.getStdout(), ""));
                break;
            case 4: // Wrong Answer
                result.setStatus("WRONG_ANSWER");
                result.setOutput(decodeBase64OrDefault(apiResult.getStdout(), ""));
                break;
            case 5: // Time Limit Exceeded
                result.setStatus("TIMEOUT");
                result.setOutput("程序执行超时");
                break;
            case 6: // Compilation Error
                result.setStatus("COMPILE_ERROR");
                debugBase64Decode(apiResult.getCompile_output()); // 临时调试
                result.setOutput(decodeBase64OrDefault(apiResult.getCompile_output(), "编译错误"));
                break;
            case 7: case 8: case 9: case 10: case 11: case 12: // Runtime Errors
                result.setStatus("RUNTIME_ERROR");
                String stderr = decodeBase64OrDefault(apiResult.getStderr(), "");
                String stdout = decodeBase64OrDefault(apiResult.getStdout(), "");
                result.setOutput(stderr.isEmpty() ? stdout : stderr);
                break;
            case 13: // Internal Error
                result.setStatus("SYSTEM_ERROR");
                result.setOutput("系统内部错误");
                break;
            case 14: // Exec Format Error
                result.setStatus("EXECUTION_ERROR");
                result.setOutput("执行格式错误");
                break;
            default:
                result.setStatus("UNKNOWN_ERROR");
                result.setOutput("未知状态: " + status.getDescription());
                break;
        }

        // 设置执行时间
        if (apiResult.getTime() != null) {
            try {
                result.setExecutionTime((long) (Double.parseDouble(apiResult.getTime()) * 1000));
            } catch (NumberFormatException e) {
                result.setExecutionTime(0);
            }
        }

        return result;
    }

    private String decodeBase64OrDefault(String base64String, String defaultValue) {
        try {
            if (base64String == null || base64String.isEmpty()) {
                return defaultValue;
            }
            return new String(Base64.getDecoder().decode(base64String));
        } catch (Exception e) {
            log.warn("Base64解码失败: {}", base64String, e);
            return base64String; // 如果解码失败，返回原字符串
        }
    }

    private boolean checkAnswer(String actualOutput, String expectedOutput) {
        if (actualOutput == null || expectedOutput == null) {
            return false;
        }

        String actual = actualOutput.trim();
        String expected = expectedOutput.trim();

        return actual.equals(expected);
    }

    private CodeExecutionResult createErrorResult(String message) {
        CodeExecutionResult result = new CodeExecutionResult();
        result.setStatus("SYSTEM_ERROR");
        result.setOutput(message);
        result.setExecutionTime(0);
        result.setCorrect(false);
        return result;
    }

    private String preprocessJavaCode(String code) {
        // 统一处理：将第一个类名改为Main
        code = code.replaceFirst("(public\\s+)?class\\s+\\w+", "public class Main");
        log.debug("Java代码预处理：统一类名为Main");
        return code;
    }

    // 临时调试方法，添加到 CodeExecutionService 中
    private void debugBase64Decode(String base64String) {
        if (base64String != null && !base64String.isEmpty()) {
            try {
                String decoded = new String(Base64.getDecoder().decode(base64String));
                log.debug("Base64解码结果: {}", decoded);
            } catch (Exception e) {
                log.debug("Base64解码失败: {}", e.getMessage());
            }
        }
    }

    // 其他内部类保持不变...
    public static class ApiExecutionResult {
        private String stdout;
        private String time;
        private Integer memory;
        private String stderr;
        private String token;
        private String compile_output;
        private String message;
        private Status status;

        // getters and setters
        public String getStdout() { return stdout; }
        public void setStdout(String stdout) { this.stdout = stdout; }
        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
        public Integer getMemory() { return memory; }
        public void setMemory(Integer memory) { this.memory = memory; }
        public String getStderr() { return stderr; }
        public void setStderr(String stderr) { this.stderr = stderr; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getCompile_output() { return compile_output; }
        public void setCompile_output(String compile_output) { this.compile_output = compile_output; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Status getStatus() { return status; }
        public void setStatus(Status status) { this.status = status; }
    }

    public static class Status {
        private Integer id;
        private String description;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class CodeExecutionResult {
        private String status;
        private String output;
        private long executionTime;
        private boolean correct;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getOutput() { return output; }
        public void setOutput(String output) { this.output = output; }
        public long getExecutionTime() { return executionTime; }
        public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
        public boolean isCorrect() { return correct; }
        public void setCorrect(boolean correct) { this.correct = correct; }
    }
}