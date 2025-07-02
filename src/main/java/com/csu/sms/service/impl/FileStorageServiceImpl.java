package com.csu.sms.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.csu.sms.model.SystemLog;
import com.csu.sms.service.FileStorageService;
import com.csu.sms.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;
    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;
    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;
    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    @Autowired
    private LogService logService;

    private OSS ossClient;

    @PostConstruct
    public void init() {
        log.info("Initializing OSSClient...");
        try {
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            log.info("OSSClient initialized for bucket: {}", bucketName);
            
            // 记录系统日志
            SystemLog systemLog = new SystemLog();
            systemLog.setLevel("INFO");
            systemLog.setType("OSS连接");
            systemLog.setTitle("阿里云OSS连接成功");
            systemLog.setContent("OSS客户端初始化成功，Bucket: " + bucketName + ", Endpoint: " + endpoint);
            systemLog.setSource("FileStorageService");
            logService.recordSystemLog(systemLog);
            
        } catch (Exception e) {
            log.error("Failed to initialize OSSClient", e);
            
            // 记录错误日志
            SystemLog errorLog = new SystemLog();
            errorLog.setLevel("ERROR");
            errorLog.setType("OSS连接");
            errorLog.setTitle("阿里云OSS连接失败");
            errorLog.setContent("OSS客户端初始化失败: " + e.getMessage());
            errorLog.setSource("FileStorageService");
            errorLog.setStackTrace(e.getStackTrace().toString());
            logService.recordSystemLog(errorLog);
        }
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
            log.info("OSSClient shut down.");
            
            // 记录系统日志
            SystemLog systemLog = new SystemLog();
            systemLog.setLevel("INFO");
            systemLog.setType("OSS连接");
            systemLog.setTitle("阿里云OSS连接关闭");
            systemLog.setContent("OSS客户端已关闭");
            systemLog.setSource("FileStorageService");
            logService.recordSystemLog(systemLog);
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Cannot upload empty file.");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileKey = folder + UUID.randomUUID().toString().replace("-", "") + fileExtension;

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            ossClient.putObject(bucketName, fileKey, file.getInputStream(), metadata);

            // 返回文件的公共访问URL。
            // 注意：如果你的Bucket设置为私有，这里需要生成签名URL，但我们之前约定了公共读。
            // 如果你使用了CDN，这里应该返回CDN域名对应的URL。
            // 假设默认OSS公共读URL格式: https://{bucketName}.{endpoint}/{fileKey}
            String fileUrl = "https://" + bucketName + "." + endpoint + "/" + fileKey;
            log.info("File uploaded to OSS: {}", fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.error("Failed to upload file to OSS (key: {}): {}", fileKey, e.getMessage(), e);
            // 向上抛出IOException，让业务层处理
            throw new IOException("Failed to upload file to cloud storage: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        String fileKey = extractFileKeyFromUrl(fileUrl, "");
        if (fileKey == null || fileKey.isEmpty()) {
            log.warn("Invalid file URL for deletion: {}", fileUrl);
            return false;
        }
        return deleteFileByKey(fileKey);
    }

    @Override
    public boolean deleteFileByKey(String fileKey) {
        if (fileKey == null || fileKey.isEmpty()) {
            log.warn("File key for deletion is empty.");
            return false;
        }
        try {
            if (ossClient.doesObjectExist(bucketName, fileKey)) { // 检查文件是否存在再删除
                ossClient.deleteObject(bucketName, fileKey);
                log.info("File deleted from OSS: {}", fileKey);
                return true;
            } else {
                log.warn("File not found in OSS for deletion: {}", fileKey);
                return true; // 认为删除成功，因为目标文件不存在
            }
        } catch (Exception e) {
            log.error("Failed to delete file from OSS (key: {}): {}", fileKey, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String extractFileKeyFromUrl(String fileUrl, String folder) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath(); // 获取路径部分，例如 /avatars/uuid.jpg
            // 去掉开头的斜杠
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            // 如果你希望更精确地匹配，可以检查path是否以folder开头
            // 但对于OSS的默认URL，直接去掉斜杠就是key了
            return path;
        } catch (Exception e) {
            log.error("Failed to extract file key from URL {}: {}", fileUrl, e.getMessage());
            return null;
        }
    }
}
