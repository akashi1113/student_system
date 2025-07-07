package com.csu.sms.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.info.MultimediaInfo;

public class VideoUtils {

    /**
     * 获取上传视频的时长（单位：秒）
     *
     * @param multipartFile Spring MVC 的 MultipartFile 对象
     * @return 视频时长（秒），如果获取失败则返回 -1
     */
    public static int getVideoDuration(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            return -1;
        }

        // 1. 将 MultipartFile 转换为临时 File 文件
        // 因为 jave-core 需要一个物理文件路径来读取信息
        File tempFile = null;
        try {
            // 创建一个临时文件
            tempFile = File.createTempFile("temp_video_", "." + getFileExtension(multipartFile.getOriginalFilename()));

            // 将上传文件的内容写入临时文件
            try (InputStream inputStream = multipartFile.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            // 2. 使用 jave-core 读取视频信息
            MultimediaObject multimediaObject = new MultimediaObject(tempFile);
            MultimediaInfo info = multimediaObject.getInfo();

            if (info != null) {
                // 时长单位是毫秒，转换为秒
                long durationInMillis = info.getDuration();
                return (int) Math.round(durationInMillis / 1000.0);
            } else {
                return -1;
            }
        } catch (Exception e) {
            // 捕获所有可能的异常，包括 IO 异常和 EncoderException
            // 在实际项目中，这里应该使用日志框架记录错误
            System.err.println("获取视频时长失败: " + e.getMessage());
            e.printStackTrace();
            return -1;
        } finally {
            // 3. 删除临时文件，非常重要！
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * 获取文件扩展名
     */
    private static String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}

