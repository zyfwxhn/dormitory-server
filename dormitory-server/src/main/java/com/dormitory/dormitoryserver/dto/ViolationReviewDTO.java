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

    // 违规原因
    private String reason;
}