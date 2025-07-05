package com.csu.sms.controller;

import com.csu.sms.common.ApiResponse;
import com.csu.sms.service.CodeExecutionService;
import com.csu.sms.service.CodeExecutionService.CodeExecutionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/code")
@Slf4j
@CrossOrigin(origins = "5173")
public class CodeExecutionController {

    @Autowired
    private CodeExecutionService codeExecutionService;

    private final RestTemplate restTemplate=new RestTemplate();


    @PostMapping("/execute")
    public ApiResponse<CodeExecutionResult> executeCode(@RequestBody CodeSubmissionRequest request) {
        log.info("收到代码执行请求: className={}, language={}", request.getClassName(), request.getLanguage());

        try {
            // 参数验证
            if (request.getCode() == null || request.getCode().trim().isEmpty()) {
                return ApiResponse.error(400, "代码不能为空");
            }

            if (request.getLanguage() == null || request.getLanguage().trim().isEmpty()) {
                return ApiResponse.error(400, "编程语言不能为空");
            }

            CodeExecutionResult result = codeExecutionService.executeCode(
                    request.getCode(),
                    request.getLanguage(),
                    request.getClassName(),
                    request.getInput(),
                    request.getExpectedOutput()
            );
            return ApiResponse.success(result);
        } catch (IllegalArgumentException e) {
            log.warn("参数错误: {}", e.getMessage());
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("代码执行异常", e);
            return ApiResponse.error(500, "代码执行失败: " + e.getMessage());
        }
    }

    // 获取支持的编程语言列表
    @GetMapping("/languages")
    public ApiResponse<java.util.List<LanguageInfo>> getSupportedLanguages() {
        java.util.List<LanguageInfo> languages = java.util.Arrays.asList(
                new LanguageInfo("java", "Java", "OpenJDK 13.0.1"),
                new LanguageInfo("python", "Python", "3.8.1"),
                new LanguageInfo("cpp", "C++", "GCC 9.2.0"),
                new LanguageInfo("c", "C", "GCC 9.2.0"),
                new LanguageInfo("javascript", "JavaScript", "Node.js 12.14.0"),
                new LanguageInfo("go", "Go", "1.13.5"),
                new LanguageInfo("rust", "Rust", "1.40.0"),
                new LanguageInfo("kotlin", "Kotlin", "1.3.70"),
                new LanguageInfo("swift", "Swift", "5.2.3")
        );
        return ApiResponse.success(languages);
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
        String className = "Main"; // 统一类名

        switch (language.toLowerCase()) {
            case "java":
                testCode = """
                    import java.util.Scanner;
                    public class Main {
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
            case "c++":
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
            case "javascript":
            case "js":
                testCode = """
                    const readline = require('readline');
                    const rl = readline.createInterface({
                        input: process.stdin,
                        output: process.stdout
                    });
                    
                    let numbers = [];
                    rl.on('line', (line) => {
                        numbers.push(parseInt(line));
                        if (numbers.length === 2) {
                            console.log(numbers[0] + numbers[1]);
                            rl.close();
                        }
                    });
                    """;
                break;
            default:
                return ApiResponse.error(400, "不支持的语言: " + language);
        }

        CodeExecutionResult result = codeExecutionService.executeCode(
                testCode, language, className, input, expectedOutput
        );

        return ApiResponse.success(result);
    }

    // 批量测试接口
    @PostMapping("/test/all")
    public ApiResponse<java.util.Map<String, CodeExecutionResult>> testAllLanguages() {
        java.util.Map<String, CodeExecutionResult> results = new java.util.HashMap<>();

        String[] languages = {"java", "python", "cpp"};
        for (String lang : languages) {
            try {
                ApiResponse<CodeExecutionResult> response = testInput(lang);
                if (response.isSuccess()) {
                    results.put(lang, response.getData());
                } else {
                    CodeExecutionResult errorResult = new CodeExecutionResult();
                    errorResult.setStatus("TEST_ERROR");
                    errorResult.setOutput(response.getMessage());
                    results.put(lang, errorResult);
                }
            } catch (Exception e) {
                CodeExecutionResult errorResult = new CodeExecutionResult();
                errorResult.setStatus("TEST_ERROR");
                errorResult.setOutput("测试异常: " + e.getMessage());
                results.put(lang, errorResult);
            }
        }

        return ApiResponse.success(results);
    }

    // 代码提交请求类
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

    // 语言信息类
    public static class LanguageInfo {
        private String code;
        private String name;
        private String version;

        public LanguageInfo(String code, String name, String version) {
            this.code = code;
            this.name = name;
            this.version = version;
        }

        // getter和setter方法
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
    }

    @GetMapping("/test/connection")
    public ApiResponse<String> testConnection() {
        try {
            String testUrl = "https://ce.judge0.com/languages";
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    testUrl, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return ApiResponse.success("API连接正常: " + response.getBody());
            } else {
                return ApiResponse.error(500, "API连接失败: " + response.getStatusCode());
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "连接异常: " + e.getMessage());
        }
    }
}