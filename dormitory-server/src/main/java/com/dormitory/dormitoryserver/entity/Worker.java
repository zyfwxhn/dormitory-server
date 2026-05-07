package com.dormitory.dormitoryserver.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 维修员实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Worker implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id; // 主键, 自增

    private String username; // 维修员工号/登录账号

    private String password; // 密码

    private String name; // 真实姓名

    private String phone; // 手机号

    private String skills; // 擅长领域

    private Integer isAvailable; // 是否在岗 (1:是, 0:否)

    private LocalDateTime createTime; // 创建时间

    private LocalDateTime updateTime; // 修改时间
}