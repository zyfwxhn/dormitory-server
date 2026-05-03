package com.dormitory.dormitoryserver.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serializable;

/**
 * 管理员审核违规下架 DTO
 */
@Data
public class ViolationReviewDTO implements Serializable {

    // 被封禁的内容主键 ID
    @NotNull(message = "内容ID不能为空")
    private Long id;

    // 违规原因（可选，当前数据库虽无字段，但可用于后续扩展或打入日志记录）
    private String reason;
}