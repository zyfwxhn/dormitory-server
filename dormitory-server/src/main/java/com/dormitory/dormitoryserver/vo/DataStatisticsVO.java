package com.dormitory.dormitoryserver.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

/**
 * 数据统计看板总览 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataStatisticsVO implements Serializable {
    // 1. 顶部指标看板
    private TodayMetricsVO todayMetrics;

    // 2. 状态分布饼图数据
    private List<StatusCountVO> statusProportion;

    // 3. 维修员排行柱状图数据
    private List<WorkerRankVO> workerRanking;
}