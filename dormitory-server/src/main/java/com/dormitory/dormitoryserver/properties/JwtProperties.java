package com.dormitory.dormitoryserver.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "dorm.jwt")
@Data
public class JwtProperties {
    /**
     * 学生端生成jwt令牌相关配置
     */
    private String studentSecretKey;
    private long studentTtl;
    private String studentTokenName;

    /**
     * 维修员端生成jwt令牌相关配置
     */
    private String workerSecretKey;
    private long workerTtl;
    private String workerTokenName;

    /**
     * 管理员端生成jwt令牌相关配置
     */
    private String adminSecretKey;
    private long adminTtl;
    private String adminTokenName;
}