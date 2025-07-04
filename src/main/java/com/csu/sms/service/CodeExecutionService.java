package com.csu.sms.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.io.*;
import java.nio.file.*;
import java.util.concurrent.*;
import javax.tools.*;

@Service
@Slf4j
public class CodeExecutionService {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + "/code_execution/";
    private static final long TIMEOUT_SECONDS = 10; // 执行超时时间
    private static final int MAX_OUTPUT_SIZE = 1024 * 1024; // 最大输出1MB

    public CodeExecutionResult executeCode(String code, String language, String className, String input, String expectedOutput) {
        CodeExecutionResult result = new CodeExecutionResult();
        String executionId = generateExecutionId();
        Path workDir = null;

        try {
            // 创建工作目录
            workDir = createWorkDirectory(executionId);

            // 根据语言执行不同的编译和运行逻辑
            switch (language.toLowerCase()) {
                case "java":
                    result = executeJavaCode(code, className, input, expectedOutput, workDir);
                    break;
                case "cpp":
                case "c++":
                    result = executeCppCode(code, input, expectedOutput, workDir);
                    break;
                case "python":
                    result = executePythonCode(code, input, expectedOutput, workDir);
                    break;
                default:
                    result.setStatus("UNSUPPORTED_LANGUAGE");
                    result.setOutput("不支持的编程语言: " + language);
                    break;
            }

        } catch (Exception e) {
            log.error("代码执行异常", e);
            result.setStatus("SYSTEM_ERROR");
            result.setOutput("系统错误: " + e.getMessage());
        } finally {
            // 清理临时文件
            cleanupWorkDirectory(workDir);
        }

        return result;
    }

    // 兼容旧版本的方法
    public CodeExecutionResult executeCode(String code, String className, String input, String expectedOutput) {
        return executeCode(code, "java", className, input, expectedOutput);
    }

    private CodeExecutionResult executeJavaCode(String code, String className, String input, String expectedOutput, Path workDir) {
        CodeExecutionResult result = new CodeExecutionResult();

        try {
            // 1. 编译Java代码
            CompilationResult compilationResult = compileJavaCode(code, className, workDir);
            if (!compilationResult.isSuccess()) {
                result.setStatus("COMPILE_ERROR");
                result.setOutput(compilationResult.getErrors());
                return result;
            }

            // 2. 运行Java代码
            ExecutionResult executionResult = runJavaCode(className, input, workDir);
            result.setStatus(executionResult.getStatus());
            result.setOutput(executionResult.getOutput());
            result.setExecutionTime(executionResult.getExecutionTime());

            // 3. 检查答案
            if (expectedOutput != null && !expectedOutput.trim().isEmpty()) {
                boolean isCorrect = checkAnswer(executionResult.getOutput(), expectedOutput);
                result.setCorrect(isCorrect);
            }

        } catch (Exception e) {
            log.error("Java代码执行异常", e);
            result.setStatus("SYSTEM_ERROR");
            result.setOutput("系统错误: " + e.getMessage());
        }

        return result;
    }

    private CodeExecutionResult executeCppCode(String code, String input, String expectedOutput, Path workDir) {
        CodeExecutionResult result = new CodeExecutionResult();
        long startTime = System.currentTimeMillis();

        try {
            // 1. 创建C++源文件
            Path sourceFile = workDir.resolve("main.cpp");
            Files.write(sourceFile, code.getBytes());

            // 2. 编译C++代码
            String executableName = System.getProperty("os.name").toLowerCase().contains("windows") ? "main.exe" : "main";
            Path executableFile = workDir.resolve(executableName);

            ProcessBuilder compileBuilder = new ProcessBuilder();
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                // Windows系统使用g++
                compileBuilder.command("g++", "-o", executableFile.toString(), sourceFile.toString());
            } else {
                // Linux/Mac系统使用g++
                compileBuilder.command("g++", "-o", executableFile.toString(), sourceFile.toString());
            }

            compileBuilder.directory(workDir.toFile());
            compileBuilder.redirectErrorStream(true);

            Process compileProcess = compileBuilder.start();

            // 读取编译输出
            StringBuilder compileOutput = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(compileProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    compileOutput.append(line).append("\n");
                }
            }

            boolean compileFinished = compileProcess.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!compileFinished) {
                compileProcess.destroyForcibly();
                result.setStatus("COMPILE_TIMEOUT");
                result.setOutput("编译超时");
                return result;
            }

            if (compileProcess.exitValue() != 0) {
                result.setStatus("COMPILE_ERROR");
                result.setOutput("编译错误:\n" + compileOutput.toString());
                return result;
            }

            // 3. 运行C++程序
            ProcessBuilder runBuilder = new ProcessBuilder();
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                runBuilder.command(executableFile.toString());
            } else {
                runBuilder.command("./" + executableName);
            }

            runBuilder.directory(workDir.toFile());
            runBuilder.redirectErrorStream(true);

            Process runProcess = runBuilder.start();

            // 提供输入
            if (input != null && !input.isEmpty()) {
                try (PrintWriter writer = new PrintWriter(runProcess.getOutputStream())) {
                    writer.print(input);
                    writer.flush();
                }
            }

            // 读取输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(runProcess.getInputStream()))) {
                String line;
                int totalChars = 0;
                while ((line = reader.readLine()) != null) {
                    if (totalChars + line.length() > MAX_OUTPUT_SIZE) {
                        output.append("\n[输出过长，已截断]");
                        break;
                    }
                    output.append(line).append("\n");
                    totalChars += line.length() + 1;
                }
            }

            boolean runFinished = runProcess.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!runFinished) {
                runProcess.destroyForcibly();
                result.setStatus("TIMEOUT");
                result.setOutput("程序执行超时（超过" + TIMEOUT_SECONDS + "秒）");
            } else {
                int exitCode = runProcess.exitValue();
                if (exitCode == 0) {
                    result.setStatus("SUCCESS");
                    result.setOutput(output.toString());
                } else {
                    result.setStatus("RUNTIME_ERROR");
                    result.setOutput("程序异常退出，退出码: " + exitCode + "\n" + output.toString());
                }
            }

            // 检查答案
            if (expectedOutput != null && !expectedOutput.trim().isEmpty()) {
                boolean isCorrect = checkAnswer(result.getOutput(), expectedOutput);
                result.setCorrect(isCorrect);
            }

        } catch (Exception e) {
            log.error("C++代码执行异常", e);
            result.setStatus("EXECUTION_ERROR");
            result.setOutput("执行异常: " + e.getMessage());
        }

        long executionTime = System.currentTimeMillis() - startTime;
        result.setExecutionTime(executionTime);

        return result;
    }

    private CodeExecutionResult executePythonCode(String code, String input, String expectedOutput, Path workDir) {
        CodeExecutionResult result = new CodeExecutionResult();
        long startTime = System.currentTimeMillis();

        try {
            // 1. 创建Python源文件
            Path sourceFile = workDir.resolve("main.py");
            Files.write(sourceFile, code.getBytes());

            // 2. 运行Python代码
            ProcessBuilder processBuilder = new ProcessBuilder();

            // 尝试使用python3，如果不存在则使用python
            String pythonCommand = "python3";
            try {
                ProcessBuilder testBuilder = new ProcessBuilder("python3", "--version");
                Process testProcess = testBuilder.start();
                testProcess.waitFor(2, TimeUnit.SECONDS);
                if (testProcess.exitValue() != 0) {
                    pythonCommand = "python";
                }
            } catch (Exception e) {
                pythonCommand = "python";
            }

            processBuilder.command(pythonCommand, sourceFile.toString());
            processBuilder.directory(workDir.toFile());
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            // 提供输入
            if (input != null && !input.isEmpty()) {
                try (PrintWriter writer = new PrintWriter(process.getOutputStream())) {
                    writer.print(input);
                    writer.flush();
                }
            }

            // 读取输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                int totalChars = 0;
                while ((line = reader.readLine()) != null) {
                    if (totalChars + line.length() > MAX_OUTPUT_SIZE) {
                        output.append("\n[输出过长，已截断]");
                        break;
                    }
                    output.append(line).append("\n");
                    totalChars += line.length() + 1;
                }
            }

            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                result.setStatus("TIMEOUT");
                result.setOutput("程序执行超时（超过" + TIMEOUT_SECONDS + "秒）");
            } else {
                int exitCode = process.exitValue();
                if (exitCode == 0) {
                    result.setStatus("SUCCESS");
                    result.setOutput(output.toString());
                } else {
                    result.setStatus("RUNTIME_ERROR");
                    result.setOutput("程序异常退出，退出码: " + exitCode + "\n" + output.toString());
                }
            }

            // 检查答案
            if (expectedOutput != null && !expectedOutput.trim().isEmpty()) {
                boolean isCorrect = checkAnswer(result.getOutput(), expectedOutput);
                result.setCorrect(isCorrect);
            }

        } catch (Exception e) {
            log.error("Python代码执行异常", e);
            result.setStatus("EXECUTION_ERROR");
            result.setOutput("执行异常: " + e.getMessage());
        }

        long executionTime = System.currentTimeMillis() - startTime;
        result.setExecutionTime(executionTime);

        return result;
    }

    private CompilationResult compileJavaCode(String code, String className, Path workDir) {
        CompilationResult result = new CompilationResult();

        try {
            // 创建源文件
            Path sourceFile = workDir.resolve(className + ".java");
            Files.write(sourceFile, code.getBytes());

            // 获取编译器
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                result.setSuccess(false);
                result.setErrors("系统未找到Java编译器");
                return result;
            }

            // 设置编译错误收集器
            StringWriter errorWriter = new StringWriter();
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

            StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

            // 设置编译选项
            Iterable<String> options = java.util.Arrays.asList(
                    "-d", workDir.toString(), // 输出目录
                    "-cp", workDir.toString() // 类路径
            );

            Iterable<? extends JavaFileObject> compilationUnits =
                    fileManager.getJavaFileObjects(sourceFile.toFile());

            // 执行编译
            JavaCompiler.CompilationTask task = compiler.getTask(
                    errorWriter, fileManager, diagnostics, options, null, compilationUnits);

            boolean success = task.call();

            if (!success) {
                StringBuilder errors = new StringBuilder();
                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                    errors.append("第").append(diagnostic.getLineNumber()).append("行: ")
                            .append(diagnostic.getMessage(null)).append("\n");
                }
                result.setErrors(errors.toString());
            }

            result.setSuccess(success);
            fileManager.close();

        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrors("编译异常: " + e.getMessage());
        }

        return result;
    }

    private ExecutionResult runJavaCode(String className, String input, Path workDir) {
        ExecutionResult result = new ExecutionResult();
        long startTime = System.currentTimeMillis();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "java", "-cp", workDir.toString(), className
            );
            processBuilder.directory(workDir.toFile());
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            // 提供输入
            if (input != null && !input.isEmpty()) {
                try (PrintWriter writer = new PrintWriter(process.getOutputStream())) {
                    writer.print(input);
                    writer.flush();
                }
            }

            // 读取输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;
                int totalChars = 0;
                while ((line = reader.readLine()) != null) {
                    if (totalChars + line.length() > MAX_OUTPUT_SIZE) {
                        output.append("\n[输出过长，已截断]");
                        break;
                    }
                    output.append(line).append("\n");
                    totalChars += line.length() + 1;
                }
            }

            // 等待执行完成，设置超时
            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                result.setStatus("TIMEOUT");
                result.setOutput("程序执行超时（超过" + TIMEOUT_SECONDS + "秒）");
            } else {
                int exitCode = process.exitValue();
                if (exitCode == 0) {
                    result.setStatus("SUCCESS");
                    result.setOutput(output.toString());
                } else {
                    result.setStatus("RUNTIME_ERROR");
                    result.setOutput("程序异常退出，退出码: " + exitCode + "\n" + output.toString());
                }
            }

        } catch (Exception e) {
            result.setStatus("EXECUTION_ERROR");
            result.setOutput("执行异常: " + e.getMessage());
        }

        long executionTime = System.currentTimeMillis() - startTime;
        result.setExecutionTime(executionTime);

        return result;
    }

    private boolean checkAnswer(String actualOutput, String expectedOutput) {
        if (actualOutput == null || expectedOutput == null) {
            return false;
        }

        // 去除首尾空白字符后比较
        String actual = actualOutput.trim();
        String expected = expectedOutput.trim();

        return actual.equals(expected);
    }

    private Path createWorkDirectory(String executionId) throws IOException {
        Path workDir = Paths.get(TEMP_DIR, executionId);
        Files.createDirectories(workDir);
        return workDir;
    }

    private String generateExecutionId() {
        return "exec_" + System.currentTimeMillis() + "_" +
                Thread.currentThread().getId();
    }

    private void cleanupWorkDirectory(Path workDir) {
        if (workDir != null && Files.exists(workDir)) {
            try {
                Files.walk(workDir)
                        .sorted((a, b) -> b.compareTo(a)) // 先删除文件，再删除目录
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                log.warn("清理临时文件失败: " + path, e);
                            }
                        });
            } catch (IOException e) {
                log.warn("清理工作目录失败: " + workDir, e);
            }
        }
    }

    // 内部类定义
    public static class CodeExecutionResult {
        private String status;
        private String output;
        private long executionTime;
        private boolean correct;

        // getter和setter方法
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getOutput() { return output; }
        public void setOutput(String output) { this.output = output; }
        public long getExecutionTime() { return executionTime; }
        public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
        public boolean isCorrect() { return correct; }
        public void setCorrect(boolean correct) { this.correct = correct; }
    }

    private static class CompilationResult {
        private boolean success;
        private String errors;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getErrors() { return errors; }
        public void setErrors(String errors) { this.errors = errors; }
    }

    private static class ExecutionResult {
        private String status;
        private String output;
        private long executionTime;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getOutput() { return output; }
        public void setOutput(String output) { this.output = output; }
        public long getExecutionTime() { return executionTime; }
        public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }
    }
}