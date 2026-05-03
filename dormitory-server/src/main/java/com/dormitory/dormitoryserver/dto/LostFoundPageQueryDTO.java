package com.dormitory.dormitoryserver.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.io.Serializable;

@Data
public class LostFoundPageQueryDTO implements Serializable {

    @Positive(message = "页码必须大于0")
    private int page;

    @Positive(message = "每页条数必须大于0")
    @Max(value = 100, message = "每页最多100条")
    private int pageSize;

    // 信息类型（0: 寻物启事, 1: 失物招领） - 可选
    private Integer type;

    // 物品分类（如：校园卡、数码产品） - 可选
    private String category;

    // 标题（用于模糊搜索） - 可选
    private String title;

    // 状态（通常学生端只会查 0:寻找中 的信息，但为了扩展性我们把它加上） - 可选
    private Integer status;

    // 发布者学生ID（个人主页"我的记录"使用，大厅不传即为查全部） - 可选
    private Long studentId;
}