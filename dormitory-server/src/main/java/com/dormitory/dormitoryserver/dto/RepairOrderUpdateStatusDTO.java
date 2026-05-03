package com.dormitory.dormitoryserver.dto;

import lombok.Data;
// 注意：JDK21 + SpringBoot3 环境下，校验注解的包名是 jakarta 开头
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 报修单状态流转（接单、更新进度、完成）DTO
 */
@Data
public class RepairOrderUpdateStatusDTO implements Serializable {

    @NotNull(message = "报修单ID不能为空")
    private Long id;

    /**
     * 目标状态 (1:已接单, 2:维修中, 3:已完成)
     */
    @NotNull(message = "目标状态不能为空")
    private Integer status;

    /**
     * 维修完工照片（多张用逗号分隔，存OSS地址）
     * 业务提示：通常只有在 status = 3 (已完成) 时，前端才会传这个字段
     */
    private String finishImages;
}