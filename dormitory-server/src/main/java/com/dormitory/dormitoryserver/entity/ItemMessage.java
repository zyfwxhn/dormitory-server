package com.dormitory.dormitoryserver.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 二手交易留言实体类
 */
@Data
public class ItemMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 关联二手商品ID */
    private Long itemId;

    /** 留言发送方学生ID */
    private Long fromStudentId;

    /** 留言接收方学生ID */
    private Long toStudentId;

    /** 留言内容 */
    private String content;

    /** 留言时间 */
    private LocalDateTime createTime;
}