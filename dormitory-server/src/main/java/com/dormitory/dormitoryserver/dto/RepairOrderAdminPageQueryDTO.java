package com.dormitory.dormitoryserver.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;

@Data
public class RepairOrderAdminPageQueryDTO implements Serializable {

    @Positive(message = "页码必须大于0")
    private int page;

    @Positive(message = "每页条数必须大于0")
    @Max(value = 100, message = "每页最多100条")
    private int pageSize;

    // 报单状态 (0:待接单 1:维修中 2:已完成) - 允许为空
    private Integer status;

    // 可选的高级筛选: 按学号搜索
    private String studentNo;
}