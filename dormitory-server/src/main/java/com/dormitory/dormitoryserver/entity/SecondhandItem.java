package com.dormitory.dormitoryserver.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 二手交易商品实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecondhandItem {

    /**
     * 主键, 自增
     */
    private Long id;

    /**
     * 卖家学生ID
     */
    private Long studentId;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品详细描述
     */
    private String description;

    /**
     * 商品分类 (如: 电子数码、生活用品、代步工具)
     */
    private String category;

    /**
     * 出售价格 (极其重要: 必须使用 BigDecimal 保证精度)
     */
    private BigDecimal price;

    /**
     * 成色 (如: 全新、9成新等)
     */
    private String conditionLevel;

    /**
     * 商品图片 (多张逗号分隔)
     */
    private String images;

    /**
     * 状态 (0: 在售, 1: 已售出, 2: 已下架)
     */
    private Integer status;

    /**
     * 发布时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    // === 关联学生信息 (查询时 JOIN 填充) ===
    private String studentName;      // 卖家姓名
    private String studentNo;        // 卖家学号
    private String studentAvatar;    // 卖家头像
}