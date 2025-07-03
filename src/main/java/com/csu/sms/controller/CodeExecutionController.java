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
                    request.getLanguage(),
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

    @PostMapping("/test/java")
    public ApiResponse<CodeExecutionResult> testJava() {
        String testCode = """
            public class HelloWorld {
                public static void main(String[] args) {
                    System.out.println("Hello, Java!");
                }
            }
            """;

        CodeExecutionResult result = codeExecutionService.executeCode(
                testCode, "java", "HelloWorld", null, "Hello, Java!"
        );

        return ApiResponse.success(result);
    }

    @PostMapping("/test/cpp")
    public ApiResponse<CodeExecutionResult> testCpp() {
        String testCode = """
            #include <iostream>
            using namespace std;
            
            int main() {
                cout << "Hello, C++!" << endl;
                return 0;
            }
            """;

        CodeExecutionResult result = codeExecutionService.executeCode(
                testCode, "cpp", "main", null, "Hello, C++!"
        );

        return ApiResponse.success(result);
    }

    @PostMapping("/test/python")
    public ApiResponse<CodeExecutionResult> testPython() {
        String testCode = """
            print("Hello, Python!")
            """;

        CodeExecutionResult result = codeExecutionService.executeCode(
                testCode, "python", "main", null, "Hello, Python!"
        );

        return ApiResponse.success(result);
    }

    @PostMapping("/test/input")
    public ApiResponse<CodeExecutionResult> testInput(@RequestParam String language) {
        String testCode = "";
        String input = "5\n3";
        String expectedOutput = "8";

        switch (language.toLowerCase()) {
            case "java":
                testCode = """
                    import java.util.Scanner;
                    public class AddNumbers {
                        public static void main(String[] args) {
                            Scanner scanner = new Scanner(System.in);
                            int a = scanner.nextInt();
                            int b = scanner.nextInt();
                            System.out.println(a + b);
                            scanner.close();
                        }
                    }
                    """;
                break;
            case "cpp":
                testCode = """
                    #include <iostream>
                    using namespace std;
                    
                    int main() {
                        int a, b;
                        cin >> a >> b;
                        cout << a + b << endl;
                        return 0;
                    }
                    """;
                break;
            case "python":
                testCode = """
                    a = int(input())
                    b = int(input())
                    print(a + b)
                    """;
                break;
            default:
                return ApiResponse.error(400, "不支持的语言: " + language);
        }

        CodeExecutionResult result = codeExecutionService.executeCode(
                testCode, language, "AddNumbers", input, expectedOutput
        );

        return ApiResponse.success(result);
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