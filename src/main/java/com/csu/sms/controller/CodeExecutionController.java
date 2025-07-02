package com.csu.sms.controller;

import com.csu.sms.common.ApiResponse;
import com.csu.sms.service.CodeExecutionService;
import com.csu.sms.service.CodeExecutionService.CodeExecutionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/code")
@Slf4j
public class CodeExecutionController {

    @Autowired
    private CodeExecutionService codeExecutionService;

    @PostMapping("/execute")
    public ApiResponse<CodeExecutionResult> executeCode(@RequestBody CodeSubmissionRequest request) {
        log.info("收到代码执行请求: className={}, language={}", request.getClassName(), request.getLanguage());

        try {
            CodeExecutionResult result = codeExecutionService.executeCode(
                    request.getCode(),
                    request.getClassName(),
                    request.getInput(),
                    request.getExpectedOutput()
            );
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("代码执行异常", e);
            return ApiResponse.error(500, "代码执行失败: " + e.getMessage());
        }
    }

    @PostMapping("/test")
    public String testBasic() {
        String testCode = """
            public class HelloWorld {
                public static void main(String[] args) {
                    System.out.println("Hello, World!");
                }
            }
            """;

        CodeExecutionResult result = codeExecutionService.executeCode(
                testCode, "HelloWorld", null, "Hello, World!"
        );

        return "测试结果: " + result.getStatus() + "\n输出: " + result.getOutput();
    }

    public static class CodeSubmissionRequest {
        private String code;
        private String className;
        private String input;
        private String expectedOutput;
        private String language;

        // getter和setter方法
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        public String getInput() { return input; }
        public void setInput(String input) { this.input = input; }
        public String getExpectedOutput() { return expectedOutput; }
        public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
    }
}