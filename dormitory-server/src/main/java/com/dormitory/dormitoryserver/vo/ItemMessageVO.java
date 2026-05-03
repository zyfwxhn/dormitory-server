package com.dormitory.dormitoryserver.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ItemMessageVO {
    private Long id;
    private Long itemId;
    private Long fromStudentId;
    private String fromStudentName;
    private Long toStudentId;
    private String toStudentName;
    private String content;
    private LocalDateTime createTime;
    /** 发消息的人是不是该商品的卖家 */
    private boolean fromSeller;
}
