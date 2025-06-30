package com.csu.sms.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface FileStorageService {
    /**
     * 上传文件到云存储
     * @param file 要上传的文件
     * @param folder 在存储桶中的文件夹路径 (例如 "avatars/", "course-covers/", "videos/")
     * @return 文件的公开可访问URL
     * @throws IOException 如果文件处理失败
     */
    String uploadFile(MultipartFile file, String folder) throws IOException;

    /**
     * 从云存储删除文件（通过文件的完整URL删除）
     * @param fileUrl 要删除的文件的完整URL
     * @return true 如果删除成功，false 否则
     */
    boolean deleteFile(String fileUrl);

    /**
     * 从云存储删除文件（通过存储桶内的文件Key删除）
     * @param fileKey 存储桶内的文件Key（例如 avatars/uuid.jpg）
     * @return true 如果删除成功，false 否则
     */
    boolean deleteFileByKey(String fileKey);

    /**
     * 从完整的URL中提取存储桶内的文件Key (对象名称)
     * @param fileUrl 文件的完整URL
     * @param folder 上传时使用的文件夹路径，用于更精确地提取
     * @return 存储桶内的文件Key (例如 "avatars/a1b2c3d4.jpg")
     */
    String extractFileKeyFromUrl(String fileUrl, String folder);
}
