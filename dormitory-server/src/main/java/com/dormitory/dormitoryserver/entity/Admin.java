package com.dormitory.dormitoryserver.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Admin implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id; // 主键, 自增

    private String username; // 管理员账号

    private String password; // 密码

    private String name; // 姓名

    private LocalDateTime createTime; // 创建时间

    private LocalDateTime updateTime; // 修改时间
}