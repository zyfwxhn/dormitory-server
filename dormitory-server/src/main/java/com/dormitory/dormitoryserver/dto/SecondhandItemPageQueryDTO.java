package com.dormitory.dormitoryserver.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SecondhandItemPageQueryDTO implements Serializable {

    @Positive(message = "页码必须大于0")
    private int page;

    @Positive(message = "每页条数必须大于0")
    @Max(value = 100, message = "每页最多100条")
    private int pageSize;

    // 商品名称 (用于模糊搜索)
    private String name;

    // 物品分类
    private String category;

    // 价格区间: 最低价
    private BigDecimal minPrice;

    // 价格区间: 最高价
    private BigDecimal maxPrice;

    // 排序模式: 0-最新发布(默认), 1-价格最低优先, 2-价格最高优先
    private Integer sortMode;

    // 状态
    private Integer status;

    // 卖家学生ID
    private Long studentId;
}