package com.dormitory.dormitoryserver.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SecondhandItemUpdateStatusDTO {

    @NotNull(message = "商品ID不能为空")
    private Long id;

    @NotNull(message = "目标状态不能为空")
    private Integer status; // 1:已售出, 2:已下架
}