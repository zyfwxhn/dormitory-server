package com.dormitory.dormitoryserver.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 生活服务预约实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceReservation implements Serializable {
    private Long id;

    // 预约学生ID
    private Long studentId;

    // 关联的设备ID
    private Long deviceId;

    // 预约日期 (年月日)
    private LocalDate reservationDate;

    // 开始时间 (时分秒)
    private LocalTime startTime;

    // 结束时间 (时分秒)
    private LocalTime endTime;

    // 预约状态（0:预约中, 1:已完成, 2:已取消）
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}