package com.dormitory.dormitoryserver.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;

@Data
public class RepairOrderPageQueryDTO implements Serializable {

    @Positive(message = "页码必须大于0")
    private int page;

    @Positive(message = "每页条数必须大于0")
    @Max(value = 100, message = "每页最多100条")
    private int pageSize;

    // 报单状态（0:待接单 1:维修中 2:已完成） - 允许为空，为空时查询全部状态
    private Integer status;

    // 注意：这里坚决不放 studentId，我们在 Service 层通过 BaseContext.getCurrentId() 动态补全
}