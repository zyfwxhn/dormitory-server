package com.dormitory.dormitoryserver.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 报修订单实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepairOrder implements Serializable {
    private Long id;
    private Long studentId;         // 报修学生ID
    private String repairType;      // 故障类型
    private String description;     // 详细故障描述
    private String images;          // 图片链接
    private String addressSnapshot; // 提交时的楼栋+宿舍号快照
    private Integer status;         // 状态：0待处理, 1已接单, 2维修中, 3已完成, 4已取消
    /**
     * 维修完工照片（多张用逗号分隔，存OSS地址）
     */
    private String finishImages;
    private Long workerId;          // 分派的维修员ID
    private Integer evaluationScore;// 评价分数
    private String evaluationContent;// 评价内容
    private LocalDateTime createTime;// 报修提交时间
    private LocalDateTime updateTime;// 状态更新时间

    // === 关联学生信息（查询时 JOIN 填充，非数据库字段） ===
    private String studentName;      // 提交人姓名
    private String studentNo;        // 提交人学号
    private String studentPhone;     // 提交人电话
}