package com.csu.sms.common;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class FileStorageUtil {

    private final Path fileStorageLocation;

    public FileStorageUtil() {
        // 默认存储路径为项目根目录下的uploads文件夹
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("无法创建文件存储目录", ex);
        }
    }

    /**
     * 存储文件到指定位置
     * @param file 上传的文件
     * @return 存储的文件路径
     * @throws IOException 文件操作异常
     */
    public String storeFile(MultipartFile file) throws IOException {
        // 生成唯一文件名
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        // 解析文件路径并保存文件
        Path targetLocation = this.fileStorageLocation.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return targetLocation.toString();
    }

    /**
     * 存储文件到指定子目录
     * @param file 上传的文件
     * @param subDir 子目录名称
     * @return 存储的文件路径
     * @throws IOException 文件操作异常
     */
    public String storeFile(MultipartFile file, String subDir) throws IOException {
        // 创建子目录
        Path subDirectory = this.fileStorageLocation.resolve(subDir);
        Files.createDirectories(subDirectory);

        // 生成唯一文件名
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        // 解析文件路径并保存文件
        Path targetLocation = subDirectory.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return targetLocation.toString();
    }

    /**
     * 删除文件
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("删除文件失败: " + filePath, e);
        }
    }

    /**
     * 获取文件存储的基础路径
     * @return 基础存储路径
     */
    public String getStorageLocation() {
        return this.fileStorageLocation.toString();
    }
}
