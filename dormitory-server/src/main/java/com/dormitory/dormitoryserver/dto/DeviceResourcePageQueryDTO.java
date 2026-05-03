package com.dormitory.dormitoryserver.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.io.Serializable;

@Data
public class DeviceResourcePageQueryDTO implements Serializable {

    @Positive(message = "页码必须大于0")
    private int page;

    @Positive(message = "每页条数必须大于0")
    @Max(value = 100, message = "每页最多100条")
    private int pageSize;

    // 所属楼栋 (可选条件)
    private String buildingNo;

    // 设备名称 (可选条件，支持模糊查询)
    private String deviceName;

    // 设备状态：1正常可用, 0故障/停用 (可选条件)
    private Integer status;
}