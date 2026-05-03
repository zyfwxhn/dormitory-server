package com.dormitory.dormitoryserver.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 学生提交服务预约 DTO
 */
@Data
public class ReservationSubmitDTO implements Serializable {

    @NotNull(message = "预约设备ID不能为空")
    private Long deviceId;

    @NotNull(message = "预约日期不能为空")
    private LocalDate reservationDate;

    @NotNull(message = "预约开始时间不能为空")
    private LocalTime startTime;

    @NotNull(message = "预约结束时间不能为空")
    private LocalTime endTime;
}