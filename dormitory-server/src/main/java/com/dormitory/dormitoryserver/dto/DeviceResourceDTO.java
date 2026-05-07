package com.dormitory.dormitoryserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serializable;

/**
 * * 设备资源 DTO
 */
@Data
public class DeviceResourceDTO implements Serializable {

    // 主键ID
    private Long id;

    // 所属楼栋
    @NotBlank(message = "楼栋号不能为空")
    private String buildingNo;

    // 设备名称
    @NotBlank(message = "设备名称不能为空")
    private String deviceName;

    // 设备状态
    // 状态 0=停用 1=正常
    @NotNull(message = "设备状态不能为空")
    private Integer status;
}