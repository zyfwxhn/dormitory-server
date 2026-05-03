package com.dormitory.dormitoryserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serializable;

/**
 * 设备资源新增与修改 DTO
 */
@Data
public class DeviceResourceDTO implements Serializable {

    // 主键 ID（修改时必须有，新增时为空）
    private Long id;

    // 所属楼栋
    @NotBlank(message = "楼栋号不能为空")
    private String buildingNo;

    // 设备名称 (如：洗衣机、吹风机)
    @NotBlank(message = "设备名称不能为空")
    private String deviceName;

    // 设备状态（1:正常可用, 0:故障/停用）
    // 默认为 1，可在新增时不传，由后端补充，或强制前端传
    @NotNull(message = "设备状态不能为空")
    private Integer status;
}