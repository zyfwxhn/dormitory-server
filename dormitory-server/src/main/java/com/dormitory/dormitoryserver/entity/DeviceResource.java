package com.dormitory.dormitoryserver.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 设备资源实体类 (如洗衣机等)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceResource implements Serializable {
    private Long id;

    // 所属楼栋
    private String buildingNo;

    // 设备名称
    private String deviceName;

    // 设备状态 (1:正常可用, 0:故障/停用)
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}