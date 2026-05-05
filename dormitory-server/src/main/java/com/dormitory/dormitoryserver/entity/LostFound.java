package com.dormitory.dormitoryserver.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 失物招领实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LostFound {

    /**
     * 主键，自增
     */
    private Long id;

    /**
     * 发布者学生ID
     */
    private Long studentId;

    /**
     * 信息类型（0: 寻物启事-我丢了东西, 1: 失物招领-我捡了东西）
     */
    private Integer type;

    /**
     * 标题
     */
    private String title;

    /**
     * 物品详情描述
     */
    private String description;

    /**
     * 物品分类（如：校园卡、数码产品、书籍资料）
     */
    private String category;

    /**
     * 丢失或捡到的地点
     */
    private String location;

    /**
     * 联系方式（手机号或微信号）
     */
    private String contactInfo;

    /**
     * 图片链接（逗号分隔，存OSS地址）
     */
    private String images;

    /**
     * 状态（0: 寻找中/待认领, 1: 已解决, 2: 已撤销）
     */
    private Integer status;

    /**
     * 发布时间
     */
    private LocalDateTime createTime;

    /**
     * 最后修改时间
     */
    private LocalDateTime updateTime;

    // === 关联学生信息（查询时 JOIN 填充） ===
    private String studentName;      // 发布人姓名
    private String studentNo;        // 发布人学号
    private String studentAvatar;    // 发布人头像
}