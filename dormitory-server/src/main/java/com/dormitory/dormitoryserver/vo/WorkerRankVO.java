package com.dormitory.dormitoryserver.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * 维修员历史完单量 Top 排行 VO (ECharts 柱状图/排行榜格式)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerRankVO implements Serializable {
    // 维修员姓名
    private String workerName;
    // 累计完成单量
    private Integer completedCount;
}