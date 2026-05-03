package com.dormitory.dormitoryserver.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * 报修单状态统计 VO (ECharts 饼图所需格式)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusCountVO implements Serializable {
    // 状态名称（例如："待派单", "维修中"）
    private String name;
    // 对应数量
    private Integer value;
}