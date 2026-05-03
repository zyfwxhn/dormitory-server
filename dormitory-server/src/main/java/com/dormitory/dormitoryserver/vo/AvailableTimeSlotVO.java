package com.dormitory.dormitoryserver.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailableTimeSlotVO {
    // 空闲时段开始时间
    private LocalTime startTime;
    // 空闲时段结束时间
    private LocalTime endTime;
}