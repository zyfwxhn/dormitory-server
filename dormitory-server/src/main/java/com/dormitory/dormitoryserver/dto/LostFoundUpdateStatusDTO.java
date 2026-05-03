package com.dormitory.dormitoryserver.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LostFoundUpdateStatusDTO {
    @NotNull(message = "信息ID不能为空")
    private Long id;

    @NotNull(message = "目标状态不能为空")
    private Integer status; // 1:已解决, 2:已撤销
}