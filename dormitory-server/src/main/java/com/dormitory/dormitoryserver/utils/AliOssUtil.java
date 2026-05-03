package com.dormitory.dormitoryserver.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.dormitory.dormitoryserver.properties.AliOssProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.UUID;

@Component
@Slf4j
public class AliOssUtil {

    @Autowired
    private AliOssProperties aliOssProperties;

    /**
     * 文件上传
     * @param inputStream 文件输入流
     * @param originalFilename 原始文件名
     * @return 阿里云 OSS 上的访问 URL
     */
    public String upload(InputStream inputStream, String originalFilename) {
        String endpoint = aliOssProperties.getEndpoint();
        String accessKeyId = aliOssProperties.getAccessKeyId();
        String accessKeySecret = aliOssProperties.getAccessKeySecret();
        String bucketName = aliOssProperties.getBucketName();

        // 1. 生成新的文件名，防止出现重名文件被覆盖 (UUID + 原扩展名)
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName = UUID.randomUUID().toString() + extension;

        // 2. 创建 OSSClient 实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 3. 上传文件流
            log.info("开始上传文件到阿里云OSS: {}", objectName);
            ossClient.putObject(bucketName, objectName, inputStream);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        // 4. 拼接并返回文件的访问路径
        // 格式: https://{bucketName}.{endpoint}/{objectName}
        String url = "https://" + bucketName + "." + endpoint + "/" + objectName;
        log.info("文件上传完成，访问URL: {}", url);
        return url;
    }
}