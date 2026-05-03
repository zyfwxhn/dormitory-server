package com.dormitory.dormitoryserver.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * 今日关键指标 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayMetricsVO implements Serializable {
    // 今日新增报修数
    private Integer newRepairCount;
    // 今日已完成维修数
    private Integer finishedRepairCount;
    // 当前系统闲置服务设备数（如可用洗衣机）
    private Integer idleDeviceCount;
}