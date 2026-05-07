package com.dormitory.dormitoryserver.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统消息通知实体类
 */
@Data
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 接收通知的学生ID */
    private Long studentId;

    /** 通知标题 */
    private String title;

    /** 通知具体内容 */
    private String content;

    /** 业务类型: 1-报修通知, 2-二手留言, 3-生活预约提醒 */
    private Integer type;

    /** 是否已读: 0-未读, 1-已读 */
    private Integer isRead;

    /** 创建/发送时间 */
    private LocalDateTime createTime;
}