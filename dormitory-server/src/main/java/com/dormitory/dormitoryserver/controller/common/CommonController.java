package com.dormitory.dormitoryserver.controller.common;

import com.dormitory.dormitoryserver.result.Result;
import com.dormitory.dormitoryserver.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

/**
 * 通用接口大厅
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 通用文件上传接口
     * @param file 前端传来的文件对象
     * @return 统一响应结果, data 是图片的访问 URL
     */
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) {
        log.info("接收到文件上传请求: {}", file.getOriginalFilename());

        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isAllowedFile(originalFilename)) {
            return Result.error("不支持的文件类型, 仅允许上传图片 (jpg/png/gif/webp/bmp)");
        }

        try {
            String url = aliOssUtil.upload(file.getInputStream(), originalFilename);
            return Result.success(url);
        } catch (IOException e) {
            log.error("文件上传异常, 文件名: {}, 原因: ", originalFilename, e);
            return Result.error("文件上传失败, 请稍后重试");
        }
    }

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp"
    );

    private boolean isAllowedFile(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1) return false;
        return ALLOWED_EXTENSIONS.contains(filename.substring(dotIndex).toLowerCase());
    }
}